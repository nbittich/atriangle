package tech.artcoded.atriangle.feign.clients.elastic;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ElasticRestPublicEndpoint")
public interface DiscoverableElasticRestFeignClient extends ElasticRestFeignClient {
}
