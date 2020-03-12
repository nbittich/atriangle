package tech.artcoded.atriangle.feign.clients.file;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("FileRestPublicEndpoint")
public interface DiscoverableFileRestFeignClient extends FileRestFeignClient {
}
