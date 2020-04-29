package tech.artcoded.atriangle.feign.clients.mongodb;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "FileRestPublicEndpoint")
public interface DiscoverableMongoDbRestFeignClient extends MongoDbRestFeignClient {}
