package tech.artcoded.atriangle.mongodbsink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import tech.artcoded.atriangle.core.kafka.KafkaConfig;
import tech.artcoded.atriangle.feign.clients.file.UrlBasedFileRestFeignClient;

@SpringBootApplication
@Import({KafkaConfig.class})
@EnableFeignClients(clients = UrlBasedFileRestFeignClient.class)
public class MongoDbSinkApplication {
  public static void main(String[] args) {
    SpringApplication.run(MongoDbSinkApplication.class, args);
  }
}
