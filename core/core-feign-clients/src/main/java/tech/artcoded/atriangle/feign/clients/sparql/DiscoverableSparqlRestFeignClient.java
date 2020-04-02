package tech.artcoded.atriangle.feign.clients.sparql;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "SparqlRestPublicEndpoint")
public interface DiscoverableSparqlRestFeignClient extends ElasticRestFeignClient {
}
