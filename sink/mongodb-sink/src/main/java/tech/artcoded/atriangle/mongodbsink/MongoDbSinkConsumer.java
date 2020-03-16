package tech.artcoded.atriangle.mongodbsink;

import com.mongodb.BasicDBObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.IdGenerators;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.KafkaEvent;
import tech.artcoded.atriangle.api.dto.MongoEvent;
import tech.artcoded.atriangle.core.kafka.ATriangleConsumer;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class MongoDbSinkConsumer implements ATriangleConsumer<String, String> {
  private final MongoTemplate mongoTemplate;
  private final FileRestFeignClient fileRestFeignClient;

  @Getter
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapperWrapper mapperWrapper;


  @Value("${out.topic}")
  @Getter
  private String outTopic;

  @Inject
  public MongoDbSinkConsumer(MongoTemplate mongoTemplate,
                             FileRestFeignClient fileRestFeignClient,
                             KafkaTemplate<String, String> kafkaTemplate,
                             ObjectMapperWrapper mapperWrapper) {
    this.mongoTemplate = mongoTemplate;
    this.fileRestFeignClient = fileRestFeignClient;
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = mapperWrapper;
  }


  @Override
  public Map<String, String> consume(ConsumerRecord<String, String> record) throws Exception {
    String mongoEvent = record.value();

    Optional<KafkaEvent> optionalKafkaEvent = mapperWrapper.deserialize(mongoEvent, KafkaEvent.class);
    KafkaEvent kafkaEvent = optionalKafkaEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));
    Optional<MongoEvent> optionalMongoEvent = mapperWrapper.deserialize(kafkaEvent.getEvent(), MongoEvent.class);
    MongoEvent event = optionalMongoEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

    ResponseEntity<ByteArrayResource> inputToSink = fileRestFeignClient.download(kafkaEvent.getInputToSink().getId());


    BasicDBObject objectToSave = BasicDBObject.parse(IOUtils.toString(inputToSink.getBody()
                                                                                 .getInputStream(), StandardCharsets.UTF_8));
    BasicDBObject saved = mongoTemplate.save(objectToSave, event.getCollection());


    log.info("saved {}", saved.toJson());

    return Map.of(IdGenerators.get(), String.format("saved kafka event %s", kafkaEvent.getId()));
  }


}
