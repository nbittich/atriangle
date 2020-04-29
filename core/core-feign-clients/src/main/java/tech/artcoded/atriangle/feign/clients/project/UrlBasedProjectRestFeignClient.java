package tech.artcoded.atriangle.feign.clients.project;

import org.springframework.cloud.openfeign.FeignClient;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;

@FeignClient(name = "ProjectRestPublicEndpoint", url = "${endpoint.project.url}")
public interface UrlBasedProjectRestFeignClient extends FileRestFeignClient {}
