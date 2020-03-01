package tech.artcoded.atriangle.eventdispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;

@SpringBootApplication
@EnableScheduling
@EnableKafka
public class EventDispatcherApplication {
  public static void main(String[] args) {
    SpringApplication.run(EventDispatcherApplication.class, args);
  }

  @Bean
  public ObjectMapperWrapper objectMapperWrapper() {
    return ObjectMapper::new;
  }
}
