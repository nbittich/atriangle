package tech.artcoded.atriangle.filesink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import tech.artcoded.atriangle.core.config.KafkaConfig;

@SpringBootApplication
@Import({KafkaConfig.class})
public class FileSinkApplication {
  public static void main(String[] args) {
    SpringApplication.run(FileSinkApplication.class, args);
  }
}
