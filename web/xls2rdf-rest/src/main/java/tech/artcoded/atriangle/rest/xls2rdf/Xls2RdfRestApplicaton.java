package tech.artcoded.atriangle.rest.xls2rdf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import tech.artcoded.atriangle.core.kafka.KafkaConfig;
import tech.artcoded.atriangle.core.rest.config.SwaggerConfig;

@SpringBootApplication
@Import({KafkaConfig.class, SwaggerConfig.class})
@EnableDiscoveryClient
public class Xls2RdfRestApplicaton {
  public static void main(String[] args) {
    SpringApplication.run(Xls2RdfRestApplicaton.class, args);
  }
}
