package tech.artcoded.atriangle.filesink;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.kafka.KafkaEvent;
import tech.artcoded.atriangle.api.kafka.RdfEvent;
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

  private final KafkaTemplate<String,String> kafkaTemplate;
  private final ObjectMapperWrapper mapperWrapper;
  @Value("${out.topic}")
  private String outTopic;

  @Inject
  public FileSinkConsumer(
    KafkaTemplate<String, String> kafkaTemplate, ObjectMapperWrapper mapperWrapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = mapperWrapper;
  }

  @Override
  public Map.Entry<String, String> consume(ConsumerRecord<String, String> record) {
    String rdfEvent = record.value();


    Optional<KafkaEvent> optionalKafkaEvent = mapperWrapper.deserialize(rdfEvent, KafkaEvent.class);
    KafkaEvent kafkaEvent = optionalKafkaEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

    Optional<RdfEvent> optionalRdfEvent = mapperWrapper.deserialize(kafkaEvent.getEvent(), RdfEvent.class);
    RdfEvent event = optionalRdfEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

    try {
      FileUtils.writeByteArrayToFile(new File(folderExtPath, kafkaEvent.getId() + ".dat"), kafkaEvent.getJson()
                                                                                           .getBytes());
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

    return Map.entry(UUID.randomUUID()
                         .toString(), mapperWrapper.serialize(Map.of("ack", "true", "id", kafkaEvent.getId())));
  }

  @Override
  @KafkaListener(topics = "${spring.kafka.template.default-topic}")
  public void sink(ConsumerRecord<String, String> record) throws Exception {
    log.info("receiving key {} value {}", record.key(), record.value());
    Map.Entry<String, String> response = consume(record);
    kafkaTemplate.send(new ProducerRecord<>(outTopic, response.getKey(), response.getValue()));
  }

}
