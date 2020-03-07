package tech.artcoded.atriangle.core.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.kafka.LogEvent;
import tech.artcoded.atriangle.api.kafka.LogEventType;

import java.util.UUID;

public interface LoggerAction {
  String DEFAULT_TOPIC_NAME = "event-sink-log";
  Logger LOGGER = LoggerFactory.getLogger(LoggerAction.class);


  KafkaTemplate<String, String> getKafkaTemplate();

  ObjectMapperWrapper MAPPER_WRAPPER = ObjectMapper::new;

  @SneakyThrows
  default void log(LogEventType eventType, String messageFormat, Object... params) {
    SendResult<String, String> response = getKafkaTemplate().send(DEFAULT_TOPIC_NAME, UUID.randomUUID()
                                                                                          .toString(),
                                                                  MAPPER_WRAPPER.serialize(
                                                                    LogEvent.builder()
                                                                            .message(String.format(messageFormat, params))
                                                                            .type(eventType)
                                                                            .build()
                                                                  ))
                                                            .get();
    LOGGER.info("response {} ", response);
  }

  default void info(String messageFormat, Object... params) {
    log(LogEventType.INFO, messageFormat, params);
  }

  default void error(String messageFormat, Object... params) {
    log(LogEventType.ERROR, messageFormat, params);
  }

  default void warn(String messageFormat, Object... params) {
    log(LogEventType.WARN, messageFormat, params);
  }

}
