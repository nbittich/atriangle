package tech.artcoded.atriangle.mongodbsink;

import com.mongodb.BasicDBObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.kafka.KafkaEvent;
import tech.artcoded.atriangle.api.kafka.MongoEvent;
import tech.artcoded.atriangle.core.kafka.ATriangleConsumer;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class MongoDbSinkConsumer implements ATriangleConsumer<String, String> {
  private final MongoTemplate mongoTemplate;
  @Getter
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapperWrapper mapperWrapper;


  @Value("${out.topic}")
  @Getter
  private String outTopic;

  @Inject
  public MongoDbSinkConsumer(MongoTemplate mongoTemplate,
                             KafkaTemplate<String, String> kafkaTemplate,
                             ObjectMapperWrapper mapperWrapper) {
    this.mongoTemplate = mongoTemplate;
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = mapperWrapper;
  }


  @Override
  public Map<String, String> consume(ConsumerRecord<String, String> record) {
    String mongoEvent = record.value();

    Optional<KafkaEvent> optionalKafkaEvent = mapperWrapper.deserialize(mongoEvent, KafkaEvent.class);
    KafkaEvent kafkaEvent = optionalKafkaEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));
    Optional<MongoEvent> optionalMongoEvent = mapperWrapper.deserialize(kafkaEvent.getEvent(), MongoEvent.class);
    MongoEvent event = optionalMongoEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));


    BasicDBObject objectToSave = BasicDBObject.parse(kafkaEvent.getJson());
    BasicDBObject saved = mongoTemplate.save(objectToSave, event.getCollection());


    log.info("saved {}", saved.toJson());

    return Map.of(UUID.randomUUID()
                      .toString(), mapperWrapper.serialize(Map.of("message", "save to mongo success",
                                                                  "id", kafkaEvent.getId())));
  }


}
