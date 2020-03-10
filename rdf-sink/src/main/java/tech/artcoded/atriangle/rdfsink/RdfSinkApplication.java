package tech.artcoded.atriangle.rdfsink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import tech.artcoded.atriangle.core.config.KafkaConfig;
import tech.artcoded.atriangle.core.config.SparqlConfig;

@SpringBootApplication
@EnableScheduling
@Import({SparqlConfig.class, KafkaConfig.class})
public class RdfSinkApplication {
  public static void main(String[] args) {
    SpringApplication.run(RdfSinkApplication.class, args);
  }
}
