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
import tech.artcoded.atriangle.api.CheckedSupplier;
import tech.artcoded.atriangle.api.IdGenerators;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.*;
import tech.artcoded.atriangle.core.kafka.KafkaEventHelper;
import tech.artcoded.atriangle.core.kafka.KafkaSink;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class MongoDbSinkConsumer implements KafkaSink<String, String> {
  private final MongoTemplate mongoTemplate;
  private final FileRestFeignClient fileRestFeignClient;
  private final KafkaEventHelper kafkaEventHelper;

  @Value("${kafka.dispatcher.mongodb-sink-topic-out")
  private String outTopic;

  @Getter
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapperWrapper mapperWrapper;
  private final BuildProperties buildProperties;


  @Inject
  public MongoDbSinkConsumer(MongoTemplate mongoTemplate,
                             FileRestFeignClient fileRestFeignClient,
                             KafkaEventHelper kafkaEventHelper,
                             KafkaTemplate<String, String> kafkaTemplate,
                             ObjectMapperWrapper mapperWrapper,
                             BuildProperties buildProperties) {
    this.mongoTemplate = mongoTemplate;
    this.fileRestFeignClient = fileRestFeignClient;
    this.kafkaEventHelper = kafkaEventHelper;
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = mapperWrapper;
    this.buildProperties = buildProperties;
  }


  @Override
  public List<KafkaMessage<String, String>> consume(ConsumerRecord<String, String> record) throws Exception {
    String mongoEvent = record.value();

    KafkaEvent kafkaEvent = kafkaEventHelper.parseKafkaEvent(mongoEvent);
    MongoEvent event = kafkaEventHelper.parseEvent(kafkaEvent, MongoEvent.class);

    ResponseEntity<ByteArrayResource> inputToSink = fileRestFeignClient.download(kafkaEvent.getInputToSink()
                                                                                           .getId());


    BasicDBObject objectToSave = BasicDBObject.parse(IOUtils.toString(inputToSink.getBody()
                                                                                 .getInputStream(), StandardCharsets.UTF_8));
    BasicDBObject saved = mongoTemplate.save(objectToSave, event.getCollection());


    log.info("saved {}", saved.toJson());

    SinkResponse sinkResponse = SinkResponse.builder()
                                            .sinkResponsestatus(SinkResponse.SinkResponseStatus.SUCCESS)
                                            .finishedDate(new Date())
                                            .response("rdf saved to the mongodb".getBytes())
                                            .responseType(EventType.MONGODB_SINK_OUT)
                                            .build();//todo think about failure..


    KafkaEvent kafkaEventForSinkOut = kafkaEventHelper.newKafkaEventBuilder(kafkaEvent.getCorrelationId(), buildProperties)
                                                      .id(IdGenerators.get())
                                                      .eventType(EventType.MONGODB_SINK_OUT)
                                                      .event(mapperWrapper.serialize(sinkResponse))
                                                      .build();

    CheckedSupplier<KafkaMessage.KafkaMessageBuilder<String, String>> builder = KafkaMessage::builder;

    return List.of(builder.safeGet()
                          .key(IdGenerators.get())
                          .value(mapperWrapper.serialize(kafkaEventForSinkOut))
                          .outTopic(outTopic)
                          .build());
  }


}
