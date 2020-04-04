package tech.artcoded.atriangle.feign.clients.sparql;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import tech.artcoded.atriangle.api.dto.RdfType;

import java.util.List;
import java.util.Map;

public interface SparqlRestFeignClient {
  @PostMapping("/create-namespace")
  ResponseEntity<String> createNamespace(@RequestParam("namespace") String namespace);

  @PostMapping("/{namespace}/load")
  ResponseEntity<String> loadRdfFile(@RequestParam("rdfFileEventId") String rdfFileEvent,
                                     @PathVariable("namespace") String namespace);

  @PostMapping("/{namespace}/insert")
  ResponseEntity<String> insertRdfAsJsonLd(@RequestBody String jsonLdModel,
                                           @PathVariable("namespace") String namespace);

  @PostMapping(value = "/convert", consumes = MediaType.TEXT_PLAIN_VALUE)
  ResponseEntity<String> convert(@RequestBody String jsonLdModel,
                                 @RequestParam("rdfFormatInput") RdfType rdfFormatInput,
                                 @RequestParam("rdfFormatOutput") RdfType rdfFormaOutput);

  @PostMapping("/ask-query")
  ResponseEntity<Boolean> askQuery(@RequestBody String askQuery, @RequestParam("namespace") String namespace);

  @PostMapping("/select-query")
  ResponseEntity<List<Map<String, String>>> selectQuery(@RequestBody String selectQuery,
                                                        @RequestParam("namespace") String namespace);

  @PostMapping("/construct-query")
  ResponseEntity<String> constructQuery(@RequestBody String constructQuery,
                                        @RequestParam("namespace") String namespace);


}
