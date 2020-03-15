package tech.artcoded.atriangle.rest.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import tech.artcoded.atriangle.core.kafka.KafkaConfig;
import tech.artcoded.atriangle.core.rest.config.SwaggerConfig;
import tech.artcoded.atriangle.feign.clients.file.DiscoverableFileRestFeignClient;
import tech.artcoded.atriangle.feign.clients.shacl.DiscoverableShaclRestFeignClient;
import tech.artcoded.atriangle.feign.clients.skosplay.SkosPlayRestFeignClient;

@SpringBootApplication
@Import({KafkaConfig.class, SwaggerConfig.class})
@EnableDiscoveryClient
@EnableFeignClients(clients = {DiscoverableFileRestFeignClient.class,
                               SkosPlayRestFeignClient.class,
                               DiscoverableShaclRestFeignClient.class})
public class ProjectRestApplicaton {
  public static void main(String[] args) {
    SpringApplication.run(ProjectRestApplicaton.class, args);
  }
}
