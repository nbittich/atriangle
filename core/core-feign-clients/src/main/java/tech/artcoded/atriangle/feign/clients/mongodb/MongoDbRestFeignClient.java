package tech.artcoded.atriangle.feign.clients.mongodb;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

public interface MongoDbRestFeignClient {
  @PostMapping("/create-collection")
  ResponseEntity<String> createCollection(@RequestParam("collectionName") String collectionName);
  @GetMapping("/collection-names")
  ResponseEntity<Set<String>> listCollections();
}
