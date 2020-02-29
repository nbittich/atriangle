package tech.artcoded.atriangle.api.kafka;

import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Map;

public interface IKafkaService {

    List<PartitionInfo> consumerPartitionInfos();

    <T> List<?> consumeEvents(Class<T> tClass);

    List<?> consumeEvents(Integer pollingSeconds, Class<?> tClass);

    Map<Class<?>, List<?>> consumeMultipleEvents(Integer pollingSeconds, Class<?>... tClasses);
}
