package tech.artcoded.atriangle.feign.clients.elastic;

import org.springframework.cloud.openfeign.FeignClient;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;

@FeignClient(name = "ElasticRestPublicEndpoint")
public interface DiscoverableElasticRestFeignClient extends ElasticRestFeignClient {
}
