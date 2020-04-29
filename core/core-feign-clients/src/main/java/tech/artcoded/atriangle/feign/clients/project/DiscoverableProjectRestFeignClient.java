package tech.artcoded.atriangle.feign.clients.project;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ProjectRestPublicEndpoint")
public interface DiscoverableProjectRestFeignClient extends ProjectRestFeignClient {}
