package tech.artcoded.atriangle.feign.clients.mongodb;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.artcoded.atriangle.api.RawJsonWrappedResponse;

import java.util.Set;

public interface MongoDbRestFeignClient {

  @PostMapping("/create-collection")
  ResponseEntity<String> createCollection(@RequestParam("collectionName") String collectionName);

  @GetMapping("/collection-names")
  ResponseEntity<Set<String>> listCollections();

  @DeleteMapping("/collection")
  ResponseEntity<String> deleteCollection(@RequestParam("collectionName") String collectionName);

  @DeleteMapping("/delete")
  ResponseEntity<String> delete(@RequestParam("collectionName") String collectionName,
                                @RequestParam("id") String id);

  @PostMapping("/save")
  ResponseEntity<RawJsonWrappedResponse> save(@RequestParam("collectionName") String collectionName, @RequestBody String objectToSave);

  @GetMapping("/all")
  ResponseEntity<Set<RawJsonWrappedResponse>> findAll(@RequestParam("collectionName") String collectionName);

  @GetMapping("/by-id")
  ResponseEntity<RawJsonWrappedResponse> findById(@RequestParam("collectionName") String collectionName,
                                       @RequestParam("id") String id);
}
