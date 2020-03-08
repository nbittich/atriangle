package tech.artcoded.atriangle.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Import;
import tech.artcoded.atriangle.core.config.KafkaConfig;
import tech.artcoded.atriangle.core.config.ZookeeperDiscoveryConfig;

@SpringBootApplication
@Import({KafkaConfig.class, ZookeeperDiscoveryConfig.class})
@EnableZuulProxy
public class RestApplication {
  public static void main(String[] args) {
    SpringApplication.run(RestApplication.class, args);
  }
}
