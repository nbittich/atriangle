package tech.artcoded.atriangle.feign.clients.shacl;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ShaclRestPublicEndpoint",
             url = "${endpoint.shacl.url}")
public interface UrlBasedShaclRestFeignClient extends ShaclRestFeignClient {
}
