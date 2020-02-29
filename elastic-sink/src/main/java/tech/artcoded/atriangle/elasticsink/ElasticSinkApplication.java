package tech.artcoded.atriangle.elasticsink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import tech.artcoded.atriangle.core.config.ElasticConfig;
import tech.artcoded.atriangle.core.config.KafkaConfig;
import tech.artcoded.atriangle.core.config.VirtuosoConfig;

@SpringBootApplication
@Import({VirtuosoConfig.class, ElasticConfig.class, KafkaConfig.class})
public class ElasticSinkApplication {
  public static void main(String[] args) {
    SpringApplication.run(ElasticSinkApplication.class, args);
  }
}
