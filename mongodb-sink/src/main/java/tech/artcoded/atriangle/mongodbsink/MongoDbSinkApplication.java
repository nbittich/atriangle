package tech.artcoded.atriangle.mongodbsink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import tech.artcoded.atriangle.core.config.KafkaConfig;

@SpringBootApplication
@Import({KafkaConfig.class})
public class MongoDbSinkApplication {
  public static void main(String[] args) {
    SpringApplication.run(MongoDbSinkApplication.class, args);
  }
}
