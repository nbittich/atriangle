package tech.artcoded.atriangle.rest.mongodb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import tech.artcoded.atriangle.core.kafka.KafkaConfig;
import tech.artcoded.atriangle.core.rest.config.SwaggerConfig;

@SpringBootApplication
@Import({KafkaConfig.class, SwaggerConfig.class})
@EnableDiscoveryClient
public class MongoDbRestApplicaton {
  public static void main(String[] args) {
    SpringApplication.run(MongoDbRestApplicaton.class, args);
  }
}
