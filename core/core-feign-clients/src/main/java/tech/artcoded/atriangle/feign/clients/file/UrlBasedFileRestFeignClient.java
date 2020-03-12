package tech.artcoded.atriangle.feign.clients.file;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "FileRestPublicEndpoint",
             url = "${endpoint.file.url}")
public interface UrlBasedFileRestFeignClient extends FileRestFeignClient {
}
