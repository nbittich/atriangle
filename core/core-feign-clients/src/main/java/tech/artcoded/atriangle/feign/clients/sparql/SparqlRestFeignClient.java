package tech.artcoded.atriangle.feign.clients.sparql;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.artcoded.atriangle.api.dto.LogEvent;
import tech.artcoded.atriangle.api.dto.RdfType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SparqlRestFeignClient {
  @PostMapping("/create-namespace")
  ResponseEntity<String> createNamespace(@RequestParam("namespace") String namespace);

  @PostMapping("/{namespace}/load")
  ResponseEntity<String> loadRdfFile(@RequestParam("rdfFileEventId") String rdfFileEvent, @PathVariable("namespace") String namespace);

  @PostMapping("/{namespace}/insert")
  ResponseEntity<String> insertRdfAsJsonLd(@RequestBody String jsonLdModel, @PathVariable("namespace") String namespace );

  @PostMapping("/convert")
  ResponseEntity<String> convert(@RequestBody String jsonLdModel, @RequestParam("rdfFormat") RdfType rdfType );


/*
  @DeleteMapping("/index")
  ResponseEntity<String> deleteIndex(@RequestParam("indexName") String indexName);

  @GetMapping("/logs-by-correlation-id")
  List<LogEvent> getLogsByCorrelationId(@RequestParam("correlationId") String correlationId);

  @DeleteMapping("/document")
  ResponseEntity<String> deleteDocument(@RequestParam("indexName") String indexName,
                                        @RequestParam("id") String uuid);

  @PostMapping("/index/{indexName}")
  ResponseEntity<String> index(@PathVariable("indexName") String indexName,
                               @RequestBody String document);*/

}
