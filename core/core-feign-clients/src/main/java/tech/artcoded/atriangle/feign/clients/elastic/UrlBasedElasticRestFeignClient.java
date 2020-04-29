package tech.artcoded.atriangle.feign.clients.elastic;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ElasticRestPublicEndpoint", url = "${endpoint.elastic.url}")
public interface UrlBasedElasticRestFeignClient extends ElasticRestFeignClient {}
