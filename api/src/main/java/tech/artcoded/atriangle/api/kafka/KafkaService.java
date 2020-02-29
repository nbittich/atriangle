package tech.artcoded.atriangle.api.kafka;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.PartitionInfo;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Slf4j
public class KafkaService implements IKafkaService {
    @Getter
    private final SimpleKafkaTemplate kafkaTemplate;
    @Getter
    private final ObjectMapperWrapper mapperHelper;

    public KafkaService(SimpleKafkaTemplate kafkaTemplate, ObjectMapperWrapper mapperHelper) {
        this.kafkaTemplate = kafkaTemplate;
        this.mapperHelper = mapperHelper;
    }

    private int randomPartition() {
        List<PartitionInfo> partitionInfos = consumerPartitionInfos();
        List<Integer> ids = IntStream.range(0, partitionInfos.size())
                                     .boxed()
                                     .collect(Collectors.toList());
        Collections.shuffle(ids);
        return partitionInfos.get(ids.get(0))
                             .partition();
    }

    @Override
    public List<PartitionInfo> consumerPartitionInfos() {
        return kafkaTemplate.getPartitionInfos();
    }


    @Override
    public <T> List<?> consumeEvents(Class<T> tClass) {
        return consumeEvents(1, tClass);
    }

    @Override
    public List<?> consumeEvents(Integer pollingSeconds, Class<?> tClass) {
        return kafkaTemplate.consume(pollingSeconds, true, messages -> {
            log.info("messages count: " + messages.count());
            List<String> records = StreamSupport.stream(messages.spliterator(), false)
                                                .peek(e -> log.info("key: " + e.key()))
                                                .map(ConsumerRecord::value)
                                                .filter(Objects::nonNull)
                                                .collect(Collectors.toList());
            return mapperHelper.deserializeList(records, tClass);
        });

    }

    @Override
    public Map<Class<?>, List<?>> consumeMultipleEvents(Integer pollingSeconds, Class<?>... tClasses) {
        Map<Class<?>, List<?>> map = new HashMap<>();
        return Arrays.stream(tClasses)
                     .distinct()
                     .map(aClass -> Map.entry(aClass, consumeEvents(pollingSeconds, aClass)))
                     .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
