package tech.artcoded.atriangle.feign.clients.file;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(url = "${endpoint.file.url}")
public interface UrlBasedRestFeignClient extends FileRestFeignClient {
}
