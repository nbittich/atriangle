package tech.artcoded.atriangle.restsink;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.kafka.*;
import tech.artcoded.atriangle.core.kafka.ATriangleConsumer;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static tech.artcoded.atriangle.api.IdGenerators.UUID_SUPPLIER;

@Component
@Slf4j
public class RestSinkConsumer implements ATriangleConsumer<String, String> {
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapperWrapper mapperWrapper;


  @Value("${out.topic}")
  private String outTopic;

  @Inject
  public RestSinkConsumer(KafkaTemplate<String, String> kafkaTemplate,
                          ObjectMapperWrapper objectMapperWrapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = objectMapperWrapper;
  }


  @Override
  public Map.Entry<String, String> consume(ConsumerRecord<String, String> record) {

    Optional<KafkaEvent> optionalKafkaEvent = mapperWrapper.deserialize(record.value(), KafkaEvent.class);
    KafkaEvent kafkaEvent = optionalKafkaEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

    Optional<RestEvent> optionalRestEvent = mapperWrapper.deserialize(kafkaEvent.getEvent(), RestEvent.class);
    RestEvent event = optionalRestEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));


    String rdfSinkEventId = UUID_SUPPLIER.get();
    String elasticSinkEventId = UUID_SUPPLIER.get();

    RdfEvent rdfSinkEvent = RdfEvent.builder()
                                    .graphUri(event.getGraphUri())
                                    .build();

    KafkaEvent kafkaEventForRdf = KafkaEvent.builder()
                                            .id(rdfSinkEventId)
                                            .json(kafkaEvent.getJson())
                                            .eventType(EventType.RDF_SINK)
                                            .event(mapperWrapper.serialize(rdfSinkEvent))
                                            .build();

    ElasticEvent elasticEvent = ElasticEvent.builder()
                                            .index(event.getElasticIndex())
                                            .createIndex(event.isCreateIndex())
                                            .settings(event.getElasticSettingsJson())
                                            .mappings(event.getElasticMappingsJson())
                                            .build();


    KafkaEvent kafkaEventForElastic = KafkaEvent.builder()
                                                .id(elasticSinkEventId)
                                                .json(kafkaEvent.getJson())
                                                .eventType(EventType.ELASTIC_SINK)
                                                .event(mapperWrapper.serialize(elasticEvent))
                                                .build();

    log.info("sending to topic dispatcher");

    ProducerRecord<String, String> rdfRecord = new ProducerRecord<>(outTopic, rdfSinkEventId, mapperWrapper.serialize(kafkaEventForRdf));
    ProducerRecord<String, String> elasticRecord = new ProducerRecord<>(outTopic, elasticSinkEventId, mapperWrapper.serialize(kafkaEventForElastic));

    log.info("sending rdf record");
    kafkaTemplate.send(rdfRecord);
    log.info("sending elastic record");
    kafkaTemplate.send(elasticRecord);

    return Map.entry(UUID.randomUUID()
                         .toString(), mapperWrapper.serialize(Map.of("message", "kafka events produced",
                                                                     "id", kafkaEvent.getId())));
  }

  @Override
  @KafkaListener(topics = "${spring.kafka.template.default-topic}")
  public void sink(ConsumerRecord<String, String> record) throws Exception {
    log.info("receiving key {} value {}", record.key(), record.value());
    consume(record);
  }


}
