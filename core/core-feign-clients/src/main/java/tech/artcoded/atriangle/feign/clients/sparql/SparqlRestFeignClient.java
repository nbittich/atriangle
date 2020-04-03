package tech.artcoded.atriangle.feign.clients.sparql;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.artcoded.atriangle.api.dto.RdfType;

import java.util.Map;

public interface SparqlRestFeignClient {
  @PostMapping("/create-namespace")
  ResponseEntity<String> createNamespace(@RequestParam("namespace") String namespace);

  @PostMapping("/{namespace}/load")
  ResponseEntity<String> loadRdfFile(@RequestParam("rdfFileEventId") String rdfFileEvent, @PathVariable("namespace") String namespace);

  @PostMapping("/{namespace}/insert")
  ResponseEntity<String> insertRdfAsJsonLd(@RequestBody String jsonLdModel, @PathVariable("namespace") String namespace );

  @PostMapping("/convert")
  ResponseEntity<String> convert(@RequestBody String jsonLdModel, @RequestParam("rdfFormat") RdfType rdfType );

  @PostMapping("/ask-query")
  ResponseEntity<Boolean> askQuery(@RequestBody String askQuery, @RequestParam("namespace") String namespace );

  @PostMapping("/select-query")
  ResponseEntity<Map<String, Object>> selectQuery(@RequestBody String selectQuery, @RequestParam("namespace") String namespace );


}
