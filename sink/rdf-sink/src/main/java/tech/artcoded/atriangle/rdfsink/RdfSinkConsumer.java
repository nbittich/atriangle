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
import tech.artcoded.atriangle.api.dto.ElasticEvent;
import tech.artcoded.atriangle.api.dto.EventType;
import tech.artcoded.atriangle.api.dto.KafkaEvent;
import tech.artcoded.atriangle.api.dto.MongoEvent;
import tech.artcoded.atriangle.api.dto.RestEvent;
import tech.artcoded.atriangle.api.dto.SinkResponse;
import tech.artcoded.atriangle.core.kafka.ATriangleConsumer;
import tech.artcoded.atriangle.core.kafka.LoggerAction;
import tech.artcoded.atriangle.core.sparql.SimpleSparqlService;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;
import tech.artcoded.atriangle.feign.clients.shacl.ShaclRestFeignClient;

import javax.inject.Inject;
import java.util.Date;
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
  private final LoggerAction loggerAction;

  @Value("${out.topic}")
  @Getter
  private String outTopic;

  @Inject
  public RdfSinkConsumer(SimpleSparqlService sparqlService,
                         FileRestFeignClient fileRestFeignClient,
                         ShaclRestFeignClient shaclRestFeignClient,
                         KafkaTemplate<String, String> kafkaTemplate,
                         ObjectMapperWrapper mapperWrapper, LoggerAction loggerAction) {
    this.sparqlService = sparqlService;
    this.fileRestFeignClient = fileRestFeignClient;
    this.shaclRestFeignClient = shaclRestFeignClient;
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = mapperWrapper;
    this.loggerAction = loggerAction;
  }

  @Override
  public Map<String, String> consume(ConsumerRecord<String, String> record) throws Exception {
    String restEvent = record.value();

    Optional<KafkaEvent> optionalKafkaEvent = mapperWrapper.deserialize(restEvent, KafkaEvent.class);
    KafkaEvent kafkaEvent = optionalKafkaEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

    Optional<RestEvent> optionalRestEvent = mapperWrapper.deserialize(kafkaEvent.getEvent(), RestEvent.class);
    RestEvent event = optionalRestEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

    ResponseEntity<ByteArrayResource> inputToSink = fileRestFeignClient.download(kafkaEvent.getInputToSink().getId());

    if (kafkaEvent.getShaclModel() != null) {
      log.info("shacl validation");
      ResponseEntity<String> validate = shaclRestFeignClient.validate(kafkaEvent.getInputToSink(), kafkaEvent.getShaclModel());
      if (validate.getStatusCodeValue() != HttpStatus.OK.value() || StringUtils.isNotEmpty(validate.getBody())) {
        log.error("validation failed {}", validate.getBody());
        loggerAction.error(kafkaEvent::getCorrelationId, String.format("validation shacl failed for event %s, result %s", kafkaEvent
          .getId(), validate
                                                                         .getBody()));
        throw new RuntimeException();
      }
    }

    loggerAction.info(kafkaEvent::getCorrelationId, "shacl validation ok");

    log.info("saving to triplestore");

    sparqlService.load(event.getNamespace(), inputToSink.getBody()
                                                        .getInputStream(), RDFFormat.forFileName(kafkaEvent.getInputToSink()
                                                                                                           .getName()));

    loggerAction.info(kafkaEvent::getCorrelationId, "saved to triplestore");

    log.info("sending other events");

    String elasticSinkEventId = IdGenerators.get();
    String mongoSinkEventId = IdGenerators.get();
    MongoEvent mongoEvent = MongoEvent.builder().collection(event.getNamespace()).build();

    KafkaEvent kafkaEventForMongo = KafkaEvent.builder()
                                              .id(mongoSinkEventId)
                                              .correlationId(kafkaEvent.getCorrelationId())
                                              .inputToSink(kafkaEvent.getInputToSink())
                                              .eventType(EventType.MONGODB_SINK)
                                              .event(mapperWrapper.serialize(mongoEvent))
                                              .build();

    SinkResponse sinkResponse = SinkResponse.builder()
                                            .sinkResponsestatus(SinkResponse.SinkResponseStatus.SUCCESS)
                                            .correlationId(kafkaEvent.getCorrelationId())
                                            .finishedDate(new Date())
                                            .response("rdf saved to the triple store")
                                            .responseType(EventType.RDF_SINK_OUT)
                                            .build();//todo think about failure..

    KafkaEvent kafkaEventForElastic = Optional.of(event).filter(RestEvent::isSinkToElastic)
                                              .map(e -> {
                                                ElasticEvent elasticEvent = ElasticEvent.builder()
                                                                                        .index(e.getElasticIndex())
                                                                                        .createIndex(true)
                                                                                        .settings(e.getElasticSettingsJson())
                                                                                        .mappings(e.getElasticMappingsJson())
                                                                                        .build();
                                                return KafkaEvent.builder()
                                                                 .id(elasticSinkEventId)
                                                                 .correlationId(kafkaEvent.getCorrelationId())
                                                                 .inputToSink(kafkaEvent.getInputToSink())
                                                                 .eventType(EventType.ELASTIC_SINK)
                                                                 .event(mapperWrapper.serialize(elasticEvent))
                                                                 .build();
                                              }).orElse(null);

    return Map.of(IdGenerators.get(), mapperWrapper.serialize(sinkResponse),
                  elasticSinkEventId, mapperWrapper.serialize(kafkaEventForElastic),
                  mongoSinkEventId, mapperWrapper.serialize(kafkaEventForMongo));
  }


}
