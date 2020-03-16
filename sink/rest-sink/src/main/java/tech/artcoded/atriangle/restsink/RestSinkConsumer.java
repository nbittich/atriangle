package tech.artcoded.atriangle.restsink;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.IdGenerators;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.ElasticEvent;
import tech.artcoded.atriangle.api.dto.EventType;
import tech.artcoded.atriangle.api.dto.KafkaEvent;
import tech.artcoded.atriangle.api.dto.MongoEvent;
import tech.artcoded.atriangle.api.dto.RdfEvent;
import tech.artcoded.atriangle.api.dto.RestEvent;
import tech.artcoded.atriangle.core.kafka.ATriangleConsumer;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class RestSinkConsumer implements ATriangleConsumer<String, String> {
  @Getter
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapperWrapper mapperWrapper;


  @Value("${out.topic}")
  @Getter
  private String outTopic;

  @Inject
  public RestSinkConsumer(KafkaTemplate<String, String> kafkaTemplate,
                          ObjectMapperWrapper objectMapperWrapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = objectMapperWrapper;
  }


  @Override
  public Map<String, String> consume(ConsumerRecord<String, String> record) {

    Optional<KafkaEvent> optionalKafkaEvent = mapperWrapper.deserialize(record.value(), KafkaEvent.class);
    KafkaEvent kafkaEvent = optionalKafkaEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

    Optional<RestEvent> optionalRestEvent = mapperWrapper.deserialize(kafkaEvent.getEvent(), RestEvent.class);
    RestEvent event = optionalRestEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));


    String rdfSinkEventId = IdGenerators.get();
    String elasticSinkEventId = IdGenerators.get();
    String mongoSinkEventId = IdGenerators.get();

    RdfEvent rdfSinkEvent = RdfEvent.builder()
                                    .namespace(event.getNamespace())
                                    .build();

    KafkaEvent kafkaEventForRdf = KafkaEvent.builder()
                                            .id(rdfSinkEventId)
                                            .correlationId(kafkaEvent.getCorrelationId())
                                            .inputToSink(kafkaEvent.getInputToSink())
                                            .shaclModel(kafkaEvent.getShaclModel())
                                            .eventType(EventType.RDF_SINK)
                                            .event(mapperWrapper.serialize(rdfSinkEvent))
                                            .build();

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


    MongoEvent mongoEvent = MongoEvent.builder().collection(event.getNamespace()).build();

    KafkaEvent kafkaEventForMongo = KafkaEvent.builder()
                                              .id(elasticSinkEventId)
                                              .correlationId(kafkaEvent.getCorrelationId())
                                              .inputToSink(kafkaEvent.getInputToSink())
                                              .eventType(EventType.MONGODB_SINK)
                                              .event(mapperWrapper.serialize(mongoEvent))
                                              .build();


    log.info("sending to topic dispatcher");

    return Map.of(rdfSinkEventId, mapperWrapper.serialize(kafkaEventForRdf));
    // TODO
    // the following must be send only if the rdfk sink worked:
    //            elasticSinkEventId, mapperWrapper.serialize(kafkaEventForElastic),
    //                  mongoSinkEventId, mapperWrapper.serialize(kafkaEventForMongo)
  }


}
