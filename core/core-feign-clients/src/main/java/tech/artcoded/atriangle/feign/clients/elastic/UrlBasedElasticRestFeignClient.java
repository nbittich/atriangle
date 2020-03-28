package tech.artcoded.atriangle.feign.clients.elastic;

import org.springframework.cloud.openfeign.FeignClient;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;

@FeignClient(name = "ElasticRestPublicEndpoint",
             url = "${endpoint.elastic.url}")
public interface UrlBasedElasticRestFeignClient extends ElasticRestFeignClient {
}
