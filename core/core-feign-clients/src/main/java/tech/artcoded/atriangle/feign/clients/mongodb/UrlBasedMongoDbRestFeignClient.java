package tech.artcoded.atriangle.feign.clients.mongodb;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "FileRestPublicEndpoint",
             url = "${endpoint.file.url}")
public interface UrlBasedMongoDbRestFeignClient extends MongoDbRestFeignClient {
}
