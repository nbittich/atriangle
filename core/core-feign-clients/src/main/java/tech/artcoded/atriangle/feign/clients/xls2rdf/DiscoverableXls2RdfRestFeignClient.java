package tech.artcoded.atriangle.feign.clients.xls2rdf;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "Xls2RdfRestPublicEndpoint")
public interface DiscoverableXls2RdfRestFeignClient extends Xls2RdfRestFeignClient {
}
