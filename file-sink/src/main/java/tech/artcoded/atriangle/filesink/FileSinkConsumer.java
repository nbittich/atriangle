package tech.artcoded.atriangle.filesink;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.kafka.KafkaEvent;
import tech.artcoded.atriangle.core.kafka.ATriangleConsumer;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class FileSinkConsumer implements ATriangleConsumer<String, String> {
  @Value("${filesink.folder.ext}")
  private String folderExtPath;

  @Getter
  private final KafkaTemplate<String,String> kafkaTemplate;
  private final ObjectMapperWrapper mapperWrapper;
  @Value("${out.topic}")
  @Getter
  private String outTopic;

  @Inject
  public FileSinkConsumer(
    KafkaTemplate<String, String> kafkaTemplate, ObjectMapperWrapper mapperWrapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = mapperWrapper;
  }

  @Override
  public Map<String, String> consume(ConsumerRecord<String, String> record) {
    String rdfEvent = record.value();


    Optional<KafkaEvent> optionalKafkaEvent = mapperWrapper.deserialize(rdfEvent, KafkaEvent.class);
    KafkaEvent kafkaEvent = optionalKafkaEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

    try {
      FileUtils.writeByteArrayToFile(new File(folderExtPath, kafkaEvent.getId() + ".dat"), kafkaEvent.getJson()
                                                                                                     .getBytes());
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

    return Map.of(UUID.randomUUID()
                      .toString(), mapperWrapper.serialize(Map.of("ack", "true", "id", kafkaEvent.getId())));
  }


}
