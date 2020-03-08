package tech.artcoded.atriangle.upload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import tech.artcoded.atriangle.core.config.KafkaConfig;
import tech.artcoded.atriangle.core.database.JpaAuditingConfiguration;

@SpringBootApplication
@Import({KafkaConfig.class, JpaAuditingConfiguration.class})
@EnableDiscoveryClient
public class FileRestApplication {
  public static void main(String[] args) {
    SpringApplication.run(FileRestApplication.class, args);
  }
}
