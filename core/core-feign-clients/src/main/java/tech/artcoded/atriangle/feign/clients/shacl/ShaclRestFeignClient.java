package tech.artcoded.atriangle.feign.clients.shacl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface ShaclRestFeignClient {

  @PostMapping(path = "/validate")
  ResponseEntity<String> validate(@RequestParam("correlationId") String correlationId,
                                  @RequestParam("shaclFileEventId") String shaclFileEventId,
                                  @RequestParam("modelFileEventId") String modelFileEventId);

  @PostMapping(path = "/test")
  ResponseEntity<String> test(@RequestParam("shaclTurtleRules") String shaclRules,
                              @RequestParam("sampleTurtleData") String sampleData);
}
