package tech.artcoded.atriangle.feign.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import tech.artcoded.atriangle.core.rest.controller.FileUploadControllerTrait;

import java.util.Map;

@FeignClient("FileRestPublicEndpoint")
public interface FileRestFeignClient extends FileUploadControllerTrait {
  @GetMapping("/ping")
  ResponseEntity<Map<String, String>> ping();
}
