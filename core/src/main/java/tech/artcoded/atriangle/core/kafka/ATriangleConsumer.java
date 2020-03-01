package tech.artcoded.atriangle.core.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import java.util.Map;

public interface ATriangleConsumer<K, V> {

   Map.Entry<K, V> consume(ConsumerRecord<K, V> record);

   void sink(ConsumerRecord<K, V> record) throws Exception;
}
