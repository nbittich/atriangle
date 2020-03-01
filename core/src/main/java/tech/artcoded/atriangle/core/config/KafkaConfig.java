package tech.artcoded.atriangle.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;

import javax.inject.Named;

import static tech.artcoded.atriangle.core.config.NamedBean.OBJECT_MAPPER_WRAPPER;

@Configuration
@Slf4j
@EnableKafka
public class KafkaConfig {

  @Bean
  @Named(OBJECT_MAPPER_WRAPPER)
  public ObjectMapperWrapper objectMapperWrapper() {
    return ObjectMapper::new;
  }


}
