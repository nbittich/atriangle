package tech.artcoded.atriangle.api.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import tech.artcoded.atriangle.api.PropertyStore;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * @author Nordine Bittich
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class SimpleKafkaTemplate implements KafkaTemplate<String, String> {
    private String topic;
    private PropertyStore producerConfig;
    private PropertyStore consumerConfig;
    private Producer<String, String> producer;
    private Consumer<String, String> consumer;
    private Serializer<String> serializerKey;
    private Serializer<String> serializerValue;
    private Deserializer<String> deserializerKey;
    private Deserializer<String> deserializerValue;

    @Override
    public List<PartitionInfo> getPartitionInfos() {
        List<PartitionInfo> partitionInfos = getProducer().partitionsFor(getTopic());
        if (partitionInfos == null || partitionInfos.isEmpty()) {
            throw new IllegalStateException("at least one partition should exist");
        }
        log.debug("Number of partition: {}", partitionInfos.size());
        return partitionInfos;
    }

    @Override
    public Future<RecordMetadata> produce(String key, String body, int partition) {
        log.info("produce message with key: " + key + " in topic " + this.getTopic());
        log.info("Partition used to produce the message: {}", partition);
        Future<RecordMetadata> send = getProducer().send(new ProducerRecord<>(this.getTopic(), partition, key, body));
        log.info("message sent.");
        return send;
    }

    @Override
    public Future<RecordMetadata> produce(String key, String body) {
        log.info("produce message with key: " + key + " in topic " + this.getTopic());
        Future<RecordMetadata> send = getProducer().send(new ProducerRecord<>(this.getTopic(), key, body));
        log.info("message sent.");
        return send;
    }

    @Override
    public <T> T consume(long pollingSeconds, boolean commit,
                         Function<ConsumerRecords<String, String>, T> transform) {
        return consume(pollingSeconds, ChronoUnit.SECONDS, commit, transform);
    }

    @Override
    public <T> T consume(long polling, ChronoUnit chronoUnit, boolean commit,
                         Function<ConsumerRecords<String, String>, T> transform) {
        ConsumerRecords<String, String> poll = getConsumer().poll(Duration.of(polling, chronoUnit));
        T converted = transform.apply(poll);
        if (commit) {
            getConsumer().commitSync();
        }
        return converted;
    }

    @Override
    public <T> T consume(long polling, ChronoUnit chronoUnit,
                         Function<ConsumerRecords<String, String>, T> transform) {
        return consume(polling, chronoUnit, true, transform);
    }

    @Override
    public <T> T consume(long pollingSeconds, Function<ConsumerRecords<String, String>, T> transform) {
        return consume(pollingSeconds, true, transform);
    }

    @Override
    public void consume(long pollingSeconds, boolean commit,
                        java.util.function.Consumer<ConsumerRecords<String, String>> consumer) {
        Function<ConsumerRecords<String, String>, Void> func = (s) -> {
            consumer.accept(s);
            return null;
        };

        consume(pollingSeconds, commit, func);
    }

    @Override
    public void consume(long pollingSeconds,
                        java.util.function.Consumer<ConsumerRecords<String, String>> consumer) {
        consume(pollingSeconds, true, consumer);
    }

    @Override
    public void consume(long polling, ChronoUnit unit,
                        java.util.function.Consumer<ConsumerRecords<String, String>> consumer) {
        Function<ConsumerRecords<String, String>, Void> func = (s) -> {
            consumer.accept(s);
            return null;
        };

        consume(polling, unit, true, func);
    }
}
