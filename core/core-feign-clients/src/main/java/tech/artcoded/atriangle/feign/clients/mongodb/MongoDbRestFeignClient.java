package tech.artcoded.atriangle.feign.clients.mongodb;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.artcoded.atriangle.api.RawJsonWrappedResponse;

import java.util.List;
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
  ResponseEntity<RawJsonWrappedResponse> save(@RequestParam("collectionName") String collectionName,
                                              @RequestBody String objectToSave);

  /**
   * e.g "{ age : { $lt : 50 } }"
   *
   * @param collectionName
   * @param query
   * @return
   */
  @PostMapping("/query")
  ResponseEntity<List<RawJsonWrappedResponse>> query(@RequestParam("collectionName") String collectionName,
                                                     @RequestBody String query);

  @GetMapping("/all")
  ResponseEntity<Set<RawJsonWrappedResponse>> findAll(@RequestParam("collectionName") String collectionName);

  @GetMapping("/by-id")
  ResponseEntity<RawJsonWrappedResponse> findById(@RequestParam("collectionName") String collectionName,
                                                  @RequestParam("id") String id);
}
