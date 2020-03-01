package tech.artcoded.atriangle.filesink;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.kafka.KafkaEvent;
import tech.artcoded.atriangle.core.kafka.ATriangleConsumer;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class FileSinkConsumer extends ATriangleConsumer<String, String> {
  @Value("${filesink.folder.ext}")
  private String folderExtPath;

  @Override
  public Map.Entry<String, String> consume(ConsumerRecord<String, String> record) {
    String rdfEvent = record.value();
    Optional<KafkaEvent> optionalKafkaEvent = mapperWrapper.deserialize(rdfEvent, KafkaEvent.class);
    KafkaEvent event = optionalKafkaEvent.orElseThrow(() -> new RuntimeException("could not parse event"));

    try {
      FileUtils.writeByteArrayToFile(new File(folderExtPath, event.getId() + ".dat"), event.getJson()
                                                                                           .getBytes());
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

    return Map.entry(UUID.randomUUID()
                         .toString(), mapperWrapper.serialize(Map.of("ack", "true", "id", event.getId())));
  }
}
