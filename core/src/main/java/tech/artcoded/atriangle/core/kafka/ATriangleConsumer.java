package tech.artcoded.atriangle.core.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;

import javax.inject.Inject;
import java.util.Map;

@Slf4j
public abstract class ATriangleConsumer<K, V> {

  @Inject
  protected KafkaTemplate<K, V> kafkaTemplate;

  @Inject
  protected ObjectMapperWrapper mapperWrapper;

  @Value("${out.topic}")
  private String outTopic;

  public abstract Map.Entry<K, V> consume(ConsumerRecord<K, V> record);

  @KafkaListener(topics = {"${spring.kafka.template.default-topic}"})
  public void sink(ConsumerRecord<K, V> record) throws Exception {
    log.info("receiving record with id {}", record.key());
    Map.Entry<K, V> response = consume(record);
    kafkaTemplate.send(new ProducerRecord<>(outTopic, response.getKey(), response.getValue()));
  }
}
