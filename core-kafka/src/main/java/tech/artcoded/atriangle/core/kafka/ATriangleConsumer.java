package tech.artcoded.atriangle.core.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import tech.artcoded.atriangle.api.CheckedFunction;

import java.util.Map;
import java.util.function.Function;

public interface ATriangleConsumer<K, V> {
  Logger LOGGER = LoggerFactory.getLogger(ATriangleConsumer.class);

  KafkaTemplate<K, V> getKafkaTemplate();

  String getOutTopic();

  Map<K, V> consume(ConsumerRecord<K, V> record);

  default Function<Map.Entry<K, V>, SendResult<K, V>> sendKafkaMessageForEachEntries() {
    return CheckedFunction.toFunction((var response) -> getKafkaTemplate().send(new ProducerRecord<>(getOutTopic(), response.getKey(), response
      .getValue()))
                                                                          .get());
  }

  @KafkaListener(topics = "${spring.kafka.template.default-topic}")
  default void sink(ConsumerRecord<K, V> record) throws Exception {
    LOGGER.info("receiving key {} value {}", record.key(), record.value());
    Map<K, V> responses = consume(record);
    responses.entrySet()
             .stream()
             .map(this.sendKafkaMessageForEachEntries())
             .forEach(result -> LOGGER.info("result {}", result.toString()));
  }

}
