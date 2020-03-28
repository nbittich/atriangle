package tech.artcoded.atriangle.rest.elastic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import tech.artcoded.atriangle.core.elastic.ElasticConfig;
import tech.artcoded.atriangle.core.kafka.KafkaConfig;
import tech.artcoded.atriangle.core.rest.config.SwaggerConfig;

@SpringBootApplication
@Import({KafkaConfig.class, SwaggerConfig.class, ElasticConfig.class})
@EnableDiscoveryClient
public class ElasticRestApplication {
  public static void main(String[] args) {
    SpringApplication.run(ElasticRestApplication.class, args);
  }
}
