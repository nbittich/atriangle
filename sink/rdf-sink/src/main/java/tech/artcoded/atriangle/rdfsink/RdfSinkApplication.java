package tech.artcoded.atriangle.rdfsink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import tech.artcoded.atriangle.core.kafka.KafkaConfig;
import tech.artcoded.atriangle.core.sparql.SparqlConfig;
import tech.artcoded.atriangle.feign.clients.file.UrlBasedFileRestFeignClient;
import tech.artcoded.atriangle.feign.clients.shacl.UrlBasedShaclRestFeignClient;

@SpringBootApplication
@EnableScheduling
@Import({SparqlConfig.class, KafkaConfig.class})
@EnableFeignClients(clients = {UrlBasedShaclRestFeignClient.class, UrlBasedFileRestFeignClient.class})
public class RdfSinkApplication {
  public static void main(String[] args) {
    SpringApplication.run(RdfSinkApplication.class, args);
  }
}
