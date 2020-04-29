package tech.artcoded.atriangle.feign.clients.xls2rdf;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "Xls2RdfRestPublicEndpoint", url = "${endpoint.xls2rdf.url}")
public interface UrlBasedXls2RdfRestFeignClient extends Xls2RdfRestFeignClient {}
