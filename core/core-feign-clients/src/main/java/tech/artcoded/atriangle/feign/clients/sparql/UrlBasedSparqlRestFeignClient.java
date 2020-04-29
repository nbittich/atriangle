package tech.artcoded.atriangle.feign.clients.sparql;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "SparqlRestPublicEndpoint", url = "${endpoint.sparql.url}")
public interface UrlBasedSparqlRestFeignClient extends SparqlRestFeignClient {}
