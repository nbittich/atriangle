package tech.artcoded.atriangle.logsink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import tech.artcoded.atriangle.core.config.KafkaConfig;
import tech.artcoded.atriangle.core.elastic.ElasticConfig;

@SpringBootApplication
@Import({ElasticConfig.class, KafkaConfig.class})
public class LogSinkApplication {
  public static void main(String[] args) {
    SpringApplication.run(LogSinkApplication.class, args);
  }
}
