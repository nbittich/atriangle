package tech.artcoded.atriangle.feign.clients.sparql;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ElasticRestPublicEndpoint",
             url = "${endpoint.elastic.url}")
public interface UrlBasedSparqlRestFeignClient extends SparqlRestFeignClient {
}
