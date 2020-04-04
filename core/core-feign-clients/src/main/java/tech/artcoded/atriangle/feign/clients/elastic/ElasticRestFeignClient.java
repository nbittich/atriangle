package tech.artcoded.atriangle.feign.clients.elastic;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.artcoded.atriangle.api.dto.LogEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ElasticRestFeignClient {

  @GetMapping("/logs-by-correlation-id")
  List<LogEvent> getLogsByCorrelationId(@RequestParam("correlationId") String correlationId);

  @PostMapping("/create-index")
  ResponseEntity<String> createIndex(@RequestParam("indexName") String indexName,
                                     @RequestParam(value = "deleteIndexIfExist",
                                                   defaultValue = "false") boolean deleteIndexIfExist,
                                     @RequestBody(required = false) String elasticConfiguration);

  @DeleteMapping("/index")
  ResponseEntity<String> deleteIndex(@RequestParam("indexName") String indexName);

  @DeleteMapping("/document")
  ResponseEntity<String> deleteDocument(@RequestParam("indexName") String indexName,
                                        @RequestParam("id") String uuid);

  @PostMapping("/index/{indexName}")
  ResponseEntity<String> index(@PathVariable("indexName") String indexName,
                               @RequestBody String document);

  @GetMapping("/indices")
  ResponseEntity<Set<String>> indices();

  /**
   * e.g:
   * {
   * "term" : { "firstname" : "nordine" }
   * }
   *
   * @param indexName
   * @param request
   * @return
   */
  @PostMapping("/search/{indexName}")
  ResponseEntity<String> search(@PathVariable("indexName") String indexName,
                                @RequestBody String request);

  @GetMapping("/all/{indexName}")
  ResponseEntity<String> findAll(@PathVariable("indexName") String indexName);

  /**
   * e.g {"index.number_of_replicas": "2"}
   *
   * @param indexName
   * @param preserveSettings
   * @param settings
   * @return
   */
  @PostMapping("/settings/{indexName}")
  ResponseEntity<String> updateSettings(@PathVariable("indexName") String indexName,
                                        @RequestParam(value = "preserveSettings",
                                                      defaultValue = "false") boolean preserveSettings,
                                        @RequestBody String settings);

  @GetMapping("/settings/{indexName}")
  ResponseEntity<String> getSettings(@PathVariable("indexName") String indexName);


  @PostMapping("/mapping/{indexName}")
  ResponseEntity<String> updateMapping(@PathVariable("indexName") String indexName, @RequestBody String mapping);

  @GetMapping("/mapping/{indexName}")
  ResponseEntity<Map<String, Object>> getMapping(@PathVariable("indexName") String indexName);
}
