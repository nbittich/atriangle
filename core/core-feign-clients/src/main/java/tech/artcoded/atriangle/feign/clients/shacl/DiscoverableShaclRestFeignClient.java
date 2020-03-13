package tech.artcoded.atriangle.feign.clients.shacl;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ShaclRestPublicEndpoint")
public interface DiscoverableShaclRestFeignClient extends ShaclRestFeignClient {
}
