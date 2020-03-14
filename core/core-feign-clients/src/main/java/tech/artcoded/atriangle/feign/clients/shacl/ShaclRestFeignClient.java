package tech.artcoded.atriangle.feign.clients.shacl;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import tech.artcoded.atriangle.api.kafka.FileEvent;

public interface ShaclRestFeignClient {

  @PostMapping(path = "/validate",
               consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<String> validate(@RequestPart("shaclFileEvent") FileEvent shaclFileEvent,
                                  @RequestPart("modelFileEvent") FileEvent modelFileEvent);

  @PostMapping(path = "/test",
               consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<String> test(@RequestParam("shaclTurtleRules") String shaclRules,
                              @RequestParam("sampleTurtleData") String sampleData);
}
