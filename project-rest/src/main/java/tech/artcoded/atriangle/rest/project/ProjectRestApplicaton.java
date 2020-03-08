package tech.artcoded.atriangle.rest.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import tech.artcoded.atriangle.core.config.KafkaConfig;
import tech.artcoded.atriangle.core.database.JpaAuditingConfiguration;
import tech.artcoded.atriangle.core.rest.config.SwaggerConfig;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;

@SpringBootApplication
@Import({KafkaConfig.class,
          JpaAuditingConfiguration.class,
          SwaggerConfig.class})
@EnableDiscoveryClient
@EnableFeignClients(basePackageClasses = {FileRestFeignClient.class})
public class ProjectRestApplicaton {
  public static void main(String[] args) {
    SpringApplication.run(ProjectRestApplicaton.class, args);
  }
}
