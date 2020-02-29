package tech.artcoded.atriangle.api.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.artcoded.atriangle.api.PropertyStore;

import java.util.List;
import java.util.Properties;

public interface KafkaHelper {
    Logger LOG = LoggerFactory.getLogger(KafkaHelper.class.getName());

    static <K, V> Producer<K, V> createProducer(PropertyStore extraProps,
                                                Class<? extends Serializer<K>> keyClass,
                                                Class<? extends Serializer<V>> valueClass) {
        Properties props = new Properties();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keyClass.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueClass.getName());
        Properties properties = PropertyStore.fromProperties(props).merge(extraProps).toProperties();
        return new KafkaProducer<>(properties);
    }

    static <K, V> Consumer<K, V> createConsumer(PropertyStore extraProps,
                                                Class<? extends Deserializer<K>> keyClass,
                                                Class<? extends Deserializer<V>> valueClass,
                                                List<String> topics) {
        Properties props = new Properties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyClass.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueClass.getName());
        Properties properties = PropertyStore.fromProperties(props).merge(extraProps).toProperties();
        KafkaConsumer<K, V> consumer = new KafkaConsumer<>(properties);

        consumer.subscribe(topics);
        return consumer;
    }


}
