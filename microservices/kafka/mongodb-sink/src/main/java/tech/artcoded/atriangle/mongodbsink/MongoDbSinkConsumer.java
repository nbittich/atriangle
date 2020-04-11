package tech.artcoded.atriangle.mongodbsink;

import com.mongodb.BasicDBObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.KafkaEvent;
import tech.artcoded.atriangle.api.dto.KafkaMessage;
import tech.artcoded.atriangle.api.dto.MongoEvent;
import tech.artcoded.atriangle.core.kafka.KafkaEventHelper;
import tech.artcoded.atriangle.core.kafka.KafkaSink;
import tech.artcoded.atriangle.core.kafka.LoggerAction;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class MongoDbSinkConsumer implements KafkaSink<String, String> {
  private final MongoTemplate mongoTemplate;
  private final FileRestFeignClient fileRestFeignClient;
  private final KafkaEventHelper kafkaEventHelper;
  private final LoggerAction loggerAction;


  @Getter
  private final KafkaTemplate<String, String> kafkaTemplate;


  @Inject
  public MongoDbSinkConsumer(MongoTemplate mongoTemplate,
                             FileRestFeignClient fileRestFeignClient,
                             KafkaEventHelper kafkaEventHelper,
                             LoggerAction loggerAction,
                             KafkaTemplate<String, String> kafkaTemplate) {
    this.mongoTemplate = mongoTemplate;
    this.fileRestFeignClient = fileRestFeignClient;
    this.kafkaEventHelper = kafkaEventHelper;
    this.loggerAction = loggerAction;
    this.kafkaTemplate = kafkaTemplate;
  }


  @Override
  public List<KafkaMessage<String, String>> consume(ConsumerRecord<String, String> record) throws Exception {
    String mongoEvent = record.value();

    KafkaEvent kafkaEvent = kafkaEventHelper.parseKafkaEvent(mongoEvent);
    MongoEvent event = kafkaEventHelper.parseEvent(kafkaEvent, MongoEvent.class);

    ResponseEntity<ByteArrayResource> inputToSink = fileRestFeignClient.download(event.getInputToSink()
                                                                                      .getId(), kafkaEvent.getCorrelationId());


    BasicDBObject objectToSave = BasicDBObject.parse(IOUtils.toString(inputToSink.getBody()
                                                                                 .getInputStream(), StandardCharsets.UTF_8));

    BasicDBObject saved = mongoTemplate.save(objectToSave, event.getCollection());


    log.info("saved {}", saved.toJson());


    loggerAction.info(kafkaEvent::getCorrelationId, "rdf saved to mongodb");

    return List.of();
  }


}
