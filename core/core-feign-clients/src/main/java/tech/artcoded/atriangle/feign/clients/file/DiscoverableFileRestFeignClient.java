package tech.artcoded.atriangle.feign.clients.file;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "FileRestPublicEndpoint")
public interface DiscoverableFileRestFeignClient extends FileRestFeignClient {}
