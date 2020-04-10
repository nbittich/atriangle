package tech.artcoded.atriangle.rest.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import tech.artcoded.atriangle.core.kafka.KafkaConfig;
import tech.artcoded.atriangle.core.rest.config.SwaggerConfig;
import tech.artcoded.atriangle.feign.clients.elastic.DiscoverableElasticRestFeignClient;
import tech.artcoded.atriangle.feign.clients.file.DiscoverableFileRestFeignClient;
import tech.artcoded.atriangle.feign.clients.shacl.DiscoverableShaclRestFeignClient;
import tech.artcoded.atriangle.feign.clients.sparql.DiscoverableSparqlRestFeignClient;
import tech.artcoded.atriangle.feign.clients.util.FeignExceptionHandler;
import tech.artcoded.atriangle.feign.clients.xls2rdf.DiscoverableXls2RdfRestFeignClient;

@SpringBootApplication
@Import({KafkaConfig.class, FeignExceptionHandler.class, SwaggerConfig.class})
@EnableDiscoveryClient
@EnableFeignClients(clients = {
  DiscoverableElasticRestFeignClient.class,
  DiscoverableFileRestFeignClient.class,
  DiscoverableXls2RdfRestFeignClient.class,
  DiscoverableShaclRestFeignClient.class,
  DiscoverableSparqlRestFeignClient.class})
@EnableCaching
public class ProjectRestApplicaton {
  public static void main(String[] args) {
    SpringApplication.run(ProjectRestApplicaton.class, args);
  }
}
