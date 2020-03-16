package tech.artcoded.atriangle.feign.clients.shacl;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tech.artcoded.atriangle.api.dto.FileEvent;

public interface ShaclRestFeignClient {

  @PostMapping(path = "/validate",
               consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<String> validate(@RequestParam("shaclFileEvent") FileEvent shaclFileEvent,
                                  @RequestParam("modelFileEvent") FileEvent modelFileEvent);

  @PostMapping(path = "/test",
               consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<String> test(@RequestParam("shaclTurtleRules") String shaclRules,
                              @RequestParam("sampleTurtleData") String sampleData);
}
