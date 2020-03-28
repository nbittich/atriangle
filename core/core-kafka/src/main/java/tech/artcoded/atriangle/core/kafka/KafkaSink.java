package tech.artcoded.atriangle.core.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import tech.artcoded.atriangle.api.CheckedFunction;
import tech.artcoded.atriangle.api.dto.KafkaMessage;

import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;

public interface KafkaSink<K, V> {
  Logger LOGGER = LoggerFactory.getLogger(KafkaSink.class);

  KafkaTemplate<K, V> getKafkaTemplate();

  List<KafkaMessage<K, V>> consume(ConsumerRecord<K, V> record) throws Exception;

  default Function<KafkaMessage<K, V>, SendResult<K, V>> sendKafkaMessageForEachEntries() {
    return CheckedFunction.toFunction((var response) -> getKafkaTemplate().send(new ProducerRecord<>(response.getOutTopic(), response.getKey(), response
      .getValue()))
                                                                          .get());
  }

  @KafkaListener(topics = "#{'${kafka.listener.topics}'.split(',')}")
  default void sink(ConsumerRecord<K, V> record) throws Exception {
    LOGGER.info("receiving key {}, partition {}, offset {}", record.key(), record.partition(), record.offset());
    List<KafkaMessage<K, V>> responses = consume(record);
    responses.stream()
             .map(this.sendKafkaMessageForEachEntries())
             .forEach(result -> LOGGER.info("result {}", result.toString()));
  }

}
