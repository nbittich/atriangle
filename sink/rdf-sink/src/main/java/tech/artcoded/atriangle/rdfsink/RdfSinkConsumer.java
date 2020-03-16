package tech.artcoded.atriangle.rdfsink;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.openrdf.rio.RDFFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.IdGenerators;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.KafkaEvent;
import tech.artcoded.atriangle.api.dto.RdfEvent;
import tech.artcoded.atriangle.core.kafka.ATriangleConsumer;
import tech.artcoded.atriangle.core.sparql.SimpleSparqlService;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;
import tech.artcoded.atriangle.feign.clients.shacl.ShaclRestFeignClient;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class RdfSinkConsumer implements ATriangleConsumer<String, String> {
  private final SimpleSparqlService sparqlService;
  private final FileRestFeignClient fileRestFeignClient;
  private final ShaclRestFeignClient shaclRestFeignClient;

  @Getter
  private final KafkaTemplate<String, String> kafkaTemplate;

  private final ObjectMapperWrapper mapperWrapper;

  @Value("${out.topic}")
  @Getter
  private String outTopic;

  @Inject
  public RdfSinkConsumer(SimpleSparqlService sparqlService,
                         FileRestFeignClient fileRestFeignClient,
                         ShaclRestFeignClient shaclRestFeignClient,
                         KafkaTemplate<String, String> kafkaTemplate,
                         ObjectMapperWrapper mapperWrapper) {
    this.sparqlService = sparqlService;
    this.fileRestFeignClient = fileRestFeignClient;
    this.shaclRestFeignClient = shaclRestFeignClient;
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = mapperWrapper;
  }

  @Override
  public Map<String, String> consume(ConsumerRecord<String, String> record) throws Exception {
    String rdfEvent = record.value();

    Optional<KafkaEvent> optionalKafkaEvent = mapperWrapper.deserialize(rdfEvent, KafkaEvent.class);
    KafkaEvent kafkaEvent = optionalKafkaEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

    Optional<RdfEvent> optionalRdfEvent = mapperWrapper.deserialize(kafkaEvent.getEvent(), RdfEvent.class);
    RdfEvent event = optionalRdfEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

    ResponseEntity<ByteArrayResource> inputToSink = fileRestFeignClient.download(kafkaEvent.getInputToSink().getId());

    log.info("shacl validation");
    ResponseEntity<String> validate = shaclRestFeignClient.validate(kafkaEvent.getInputToSink(), kafkaEvent.getShaclModel());
    if (validate.getStatusCodeValue() != HttpStatus.OK.value() || StringUtils.isNotEmpty(validate.getBody())) {
      log.error("validation failed {}", validate.getBody());
      return Map.of(IdGenerators.get(), String.format("validation shacl failed for event %s, result %s", kafkaEvent.getId(), validate
        .getBody()));
    }

    log.info("saving to triplestore");

    sparqlService.load(event.getNamespace(), inputToSink.getBody()
                                                        .getInputStream(), RDFFormat.forFileName(kafkaEvent.getInputToSink()
                                                                                                           .getName()));

    log.info("saved to triplestore");

    return Map.of(IdGenerators.get(), String.format("kafka event %s saved in triplestore", kafkaEvent.getId()));
  }


}
