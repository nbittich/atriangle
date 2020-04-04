package tech.artcoded.atriangle.core.kafka;

import org.apache.kafka.common.header.Headers;
import org.springframework.boot.info.BuildProperties;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.KafkaEvent;
import tech.artcoded.atriangle.api.dto.KafkaEventMetadata;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public interface KafkaEventHelper {

  ObjectMapperWrapper getObjectMapperWrapper();

  default KafkaEvent parseKafkaEvent(String event) {
    return parseEvent(event, KafkaEvent.class);
  }

  default <T> T parseEvent(String event, Class<T> tClass) {
    Optional<T> optionalEvent = getObjectMapperWrapper().deserialize(event, tClass);
    return optionalEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));
  }

  default <T> T parseEvent(KafkaEvent event, Class<T> tClass) {
    return parseEvent(event.getEvent(), tClass);
  }

  default KafkaEvent.KafkaEventBuilder copyKafkaEventBuilder(KafkaEvent kafkaEvent, BuildProperties buildProperties) {
    return kafkaEvent.toBuilder()
                     .eventMetadata(KafkaEventMetadata.builder()
                                                      .version(buildProperties.getVersion())
                                                      .artifactId(buildProperties.getArtifact())
                                                      .groupId(buildProperties.getGroup())
                                                      .moduleName(buildProperties.getName())
                                                      .build());
  }

  default KafkaEvent.KafkaEventBuilder newKafkaEventBuilder(String correlationId,
                                                            int partition,
                                                            long offset,
                                                            Headers headers,
                                                            BuildProperties buildProperties) {
    Map<String, String> headersMap = StreamSupport.stream(headers.spliterator(), false)
                                                  .map(header -> Map.entry(header.key(), new String(header.value())))
                                                  .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return KafkaEvent.builder()
                     .correlationId(correlationId)
                     .eventMetadata(KafkaEventMetadata.builder()
                                                      .version(buildProperties.getVersion())
                                                      .artifactId(buildProperties.getArtifact())
                                                      .groupId(buildProperties.getGroup())
                                                      .moduleName(buildProperties.getName())
                                                      .partition(partition)
                                                      .offset(offset)
                                                      .headers(headersMap)
                                                      .build());
  }

  default KafkaEvent.KafkaEventBuilder newKafkaEventBuilderWithoutRecord(String correlationId,
                                                                         BuildProperties buildProperties) {
    return KafkaEvent.builder()
                     .correlationId(correlationId)
                     .eventMetadata(KafkaEventMetadata.builder()
                                                      .version(buildProperties.getVersion())
                                                      .artifactId(buildProperties.getArtifact())
                                                      .groupId(buildProperties.getGroup())
                                                      .moduleName(buildProperties.getName())
                                                      .build());
  }
}
