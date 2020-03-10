package tech.artcoded.atriangle.rdfsink;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.openrdf.rio.RDFFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.SimpleSparqlService;
import tech.artcoded.atriangle.api.kafka.KafkaEvent;
import tech.artcoded.atriangle.api.kafka.RdfEvent;
import tech.artcoded.atriangle.core.kafka.ATriangleConsumer;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class RdfSinkConsumer implements ATriangleConsumer<String, String> {
  private final SimpleSparqlService sparqlService;

  @Getter
  private final KafkaTemplate<String, String> kafkaTemplate;

  private final ObjectMapperWrapper mapperWrapper;

  @Value("${out.topic}")
  @Getter
  private String outTopic;

  @Inject
  public RdfSinkConsumer(SimpleSparqlService sparqlService,
                         KafkaTemplate<String, String> kafkaTemplate,
                         ObjectMapperWrapper mapperWrapper) {
    this.sparqlService = sparqlService;
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = mapperWrapper;
  }

  @Override
  public Map<String, String> consume(ConsumerRecord<String, String> record) {
    String rdfEvent = record.value();

    Optional<KafkaEvent> optionalKafkaEvent = mapperWrapper.deserialize(rdfEvent, KafkaEvent.class);
    KafkaEvent kafkaEvent = optionalKafkaEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

    Optional<RdfEvent> optionalRdfEvent = mapperWrapper.deserialize(kafkaEvent.getEvent(), RdfEvent.class);
    RdfEvent event = optionalRdfEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

    log.info("converting to model");

    log.info("saving to triplestore");

    sparqlService.load(event.getGraphUri(), IOUtils.toInputStream(kafkaEvent.getJson(), StandardCharsets.UTF_8), RDFFormat.JSONLD);

    log.info("saved to triplestore");

    return Map.of(UUID.randomUUID()
                      .toString(), mapperWrapper.serialize(Map.of("ack", "true",
                                                                  "id", kafkaEvent.getId())));
  }


}
