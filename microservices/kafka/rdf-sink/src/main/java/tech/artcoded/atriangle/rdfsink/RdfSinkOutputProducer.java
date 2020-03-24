package tech.artcoded.atriangle.rdfsink;

import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.IdGenerators;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.ElasticEvent;
import tech.artcoded.atriangle.api.dto.EventType;
import tech.artcoded.atriangle.api.dto.FileEvent;
import tech.artcoded.atriangle.api.dto.KafkaEvent;
import tech.artcoded.atriangle.api.dto.MongoEvent;
import tech.artcoded.atriangle.api.dto.RestEvent;
import tech.artcoded.atriangle.api.dto.SinkResponse;
import tech.artcoded.atriangle.core.kafka.KafkaEventHelper;

import javax.inject.Inject;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * in case rdf sink is successfull, we produce new events to sink into mongodb/elastic
 * this could also be done in the project layer
 */
@Component
public class RdfSinkOutputProducer {

  private final ObjectMapperWrapper mapperWrapper;
  private final BuildProperties buildProperties;
  private final KafkaEventHelper kafkaEventHelper;

  @Inject
  public RdfSinkOutputProducer(ObjectMapperWrapper mapperWrapper,
                               BuildProperties buildProperties,
                               KafkaEventHelper kafkaEventHelper) {
    this.mapperWrapper = mapperWrapper;
    this.buildProperties = buildProperties;
    this.kafkaEventHelper = kafkaEventHelper;
  }

  public Map<String, String> produce(KafkaEvent kafkaEvent, RestEvent event, FileEvent jsonLdFile) {

    KafkaEvent.KafkaEventBuilder kafkaEventBuilder = kafkaEventHelper.newKafkaEventBuilder(buildProperties);
    String elasticSinkEventId = IdGenerators.get();
    String mongoSinkEventId = IdGenerators.get();
    MongoEvent mongoEvent = MongoEvent.builder().collection(event.getNamespace()).build();
    KafkaEvent kafkaEventForMongo = kafkaEventBuilder
      .id(mongoSinkEventId)
      .eventType(EventType.MONGODB_SINK)
      .inputToSink(jsonLdFile)
      .event(mapperWrapper.serialize(mongoEvent))
      .build();

    SinkResponse sinkResponse = SinkResponse.builder()
                                            .sinkResponsestatus(SinkResponse.SinkResponseStatus.SUCCESS)
                                            .correlationId(kafkaEvent.getCorrelationId())
                                            .finishedDate(new Date())
                                            .response(mapperWrapper.serialize(jsonLdFile).getBytes())
                                            .responseType(EventType.RDF_SINK_OUT)
                                            .build();//todo think about failure..


    KafkaEvent kafkaEventForSinkOut = kafkaEventBuilder
      .id(IdGenerators.get())
      .eventType(EventType.RDF_SINK_OUT)
      .inputToSink(jsonLdFile)
      .event(mapperWrapper.serialize(sinkResponse))
      .build();

    KafkaEvent kafkaEventForElastic = Optional.of(event).filter(RestEvent::isSinkToElastic)
                                              .map(e -> {
                                                ElasticEvent elasticEvent = ElasticEvent.builder()
                                                                                        .index(e.getElasticIndex())
                                                                                        .createIndex(true)
                                                                                        .settings(e.getElasticSettingsJson())
                                                                                        .mappings(e.getElasticMappingsJson())
                                                                                        .build();
                                                return kafkaEventBuilder
                                                  .id(elasticSinkEventId)
                                                  .eventType(EventType.ELASTIC_SINK)
                                                  .inputToSink(jsonLdFile)
                                                  .event(mapperWrapper.serialize(elasticEvent))
                                                  .build();
                                              }).orElse(null);
    return Stream.of(Map.entry(IdGenerators.get(), mapperWrapper.serialize(kafkaEventForSinkOut)),
                     Map.entry(elasticSinkEventId, mapperWrapper.serialize(kafkaEventForElastic)),
                     Map.entry(mongoSinkEventId, mapperWrapper.serialize(kafkaEventForMongo)))
                 .filter(e -> e.getValue() != null) // filter optional event
                 .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
