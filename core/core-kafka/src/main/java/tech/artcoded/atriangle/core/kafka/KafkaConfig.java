package tech.artcoded.atriangle.core.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;

import javax.inject.Inject;
import javax.inject.Named;

import static tech.artcoded.atriangle.core.kafka.NamedBean.KAFKA_EVENT_HELPER;
import static tech.artcoded.atriangle.core.kafka.NamedBean.LOGGER_ACTION;
import static tech.artcoded.atriangle.core.kafka.NamedBean.OBJECT_MAPPER_WRAPPER;

@Configuration
@Slf4j
@EnableKafka
public class KafkaConfig {

  private final KafkaTemplate<String, String> kafkaTemplate;

  @Inject
  public KafkaConfig(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Bean
  @Named(OBJECT_MAPPER_WRAPPER)
  public ObjectMapperWrapper objectMapperWrapper() {
    return ObjectMapper::new;
  }

  @Bean
  @Named(KAFKA_EVENT_HELPER)
  public KafkaEventHelper kafkaEventHelper() {
    return this::objectMapperWrapper;
  }

  @Bean
  @Named(LOGGER_ACTION)
  public LoggerAction loggerAction() {
    return () -> kafkaTemplate;
  }

}
