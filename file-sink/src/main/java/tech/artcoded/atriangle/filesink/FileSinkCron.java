package tech.artcoded.atriangle.filesink;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.kafka.KafkaEvent;
import tech.artcoded.atriangle.api.kafka.SimpleKafkaTemplate;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class FileSinkCron {
  private final SimpleKafkaTemplate kafkaTemplate;
  private final ObjectMapperWrapper mapperWrapper;


  @Value("${filesink.folder.ext}")
  private String folderExtPath;

  @Inject
  public FileSinkCron(SimpleKafkaTemplate kafkaTemplate, ObjectMapperWrapper mapperWrapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = mapperWrapper;
  }

  @Scheduled(cron = "${filesink.cron}")
  public void sink() {
    log.info("sink started...");
    kafkaTemplate.consume(300L, ChronoUnit.MILLIS, consumerRecords -> {
      consumerRecords.forEach(record -> {
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

        kafkaTemplate.produce(UUID.randomUUID()
                                  .toString(), mapperWrapper.serialize(Map.of("ack", "true",
                                                                              "id", event.getId())));

      });
    });
    log.info("sink ended...");

  }
}
