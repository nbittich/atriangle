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
import tech.artcoded.atriangle.api.IdGenerators;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.EventType;
import tech.artcoded.atriangle.api.dto.KafkaEvent;
import tech.artcoded.atriangle.api.dto.MongoEvent;
import tech.artcoded.atriangle.api.dto.SinkResponse;
import tech.artcoded.atriangle.core.kafka.ATriangleConsumer;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Date;
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
  private final BuildProperties buildProperties;


  @Value("${out.topic}")
  @Getter
  private String outTopic;

  @Inject
  public MongoDbSinkConsumer(MongoTemplate mongoTemplate,
                             FileRestFeignClient fileRestFeignClient,
                             KafkaTemplate<String, String> kafkaTemplate,
                             ObjectMapperWrapper mapperWrapper, BuildProperties buildProperties) {
    this.mongoTemplate = mongoTemplate;
    this.fileRestFeignClient = fileRestFeignClient;
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = mapperWrapper;
    this.buildProperties = buildProperties;
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

    SinkResponse sinkResponse = SinkResponse.builder()
                                            .sinkResponsestatus(SinkResponse.SinkResponseStatus.SUCCESS)
                                            .correlationId(kafkaEvent.getCorrelationId())
                                            .finishedDate(new Date())
                                            .response("rdf saved to the mongodb".getBytes())
                                            .responseType(EventType.MONGODB_SINK_OUT)
                                            .build();//todo think about failure..


    KafkaEvent kafkaEventForSinkOut = kafkaEvent.toBuilder()
                                                .version(buildProperties.getVersion())
                                                .artifactId(buildProperties.getArtifact())
                                                .groupId(buildProperties.getGroup())
                                                .moduleName(buildProperties.getName())
                                                .id(IdGenerators.get())
                                                .eventType(EventType.MONGODB_SINK_OUT)
                                                .event(mapperWrapper.serialize(sinkResponse))
                                                .build();

    return Map.of(IdGenerators.get(), mapperWrapper.serialize(kafkaEventForSinkOut));
  }


}