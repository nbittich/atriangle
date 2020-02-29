package tech.artcoded.atriangle.api.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import tech.artcoded.atriangle.api.PropertyStore;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

public interface KafkaTemplate<K, V> {
  List<PartitionInfo> getPartitionInfos();

  Future<RecordMetadata> produce(K key, V body, int partition);

  Future<RecordMetadata> produce(K key, V body);

  <T> T consume(long pollingSeconds, boolean commit, Function<ConsumerRecords<K, V>, T> transform);

  <T> T consume(long polling, ChronoUnit chronoUnit, boolean commit, Function<ConsumerRecords<K, V>, T> transform);

  <T> T consume(long polling, ChronoUnit chronoUnit,
                Function<ConsumerRecords<String, String>, T> transform);

  <T> T consume(long pollingSeconds, Function<ConsumerRecords<K, V>, T> transform);

  void consume(long pollingSeconds, boolean commit,
               java.util.function.Consumer<ConsumerRecords<K, V>> consumer);

  String getTopic();

  void consume(long pollingSeconds, java.util.function.Consumer<ConsumerRecords<String, String>> consumer);

  PropertyStore getProducerConfig();

  PropertyStore getConsumerConfig();

  Producer<K, V> getProducer();

  Consumer<K, V> getConsumer();

  Serializer<K> getSerializerKey();

  Serializer<V> getSerializerValue();

  Deserializer<K> getDeserializerKey();

  Deserializer<V> getDeserializerValue();

  void setTopic(String topic);

  void setProducerConfig(PropertyStore producerConfig);

  void setConsumerConfig(PropertyStore consumerConfig);

  void setProducer(Producer<K, V> producer);

  void setConsumer(Consumer<K, V> consumer);

  void setSerializerKey(Serializer<K> serializerKey);

  void setSerializerValue(Serializer<V> serializerValue);

  void setDeserializerKey(Deserializer<K> deserializerKey);

  void setDeserializerValue(Deserializer<V> deserializerValue);

  void consume(long polling, ChronoUnit unit,
               java.util.function.Consumer<ConsumerRecords<String, String>> consumer);
}
