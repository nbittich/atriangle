package tech.artcoded.atriangle.feign.clients.elastic;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.artcoded.atriangle.api.dto.LogEvent;

import java.util.List;

public interface ElasticRestFeignClient {

  @GetMapping
  List<LogEvent> getLogsByCorrelationId(@RequestParam("correlationId") String correlationId);

  @PostMapping("/create-index")
  ResponseEntity<String> createIndex(@RequestParam("indexName") String indexName,
                                     @RequestParam(value = "deleteIndexIfExist", defaultValue = "false") boolean deleteIndexIfExist,
                                     @RequestBody(required = false) String elasticConfiguration);

  @DeleteMapping
  ResponseEntity<String> deleteIndex(@RequestParam("indexName") String indexName);

  @DeleteMapping("/entity")
  ResponseEntity<String> deleteEntity(@RequestParam("indexName") String indexName,
                                      @RequestParam("id") String uuid);

  @PostMapping("/index/{indexName}")
  ResponseEntity<String> index(@PathVariable("indexName") String indexName,
                               @RequestBody String entityToIndex);
}
