package tech.artcoded.atriangle.feign.clients.file;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.kafka.FileEvent;
import tech.artcoded.atriangle.api.kafka.FileEventType;

import java.util.Map;

public interface FileRestFeignClient {
  @GetMapping("/ping")
  ResponseEntity<Map<String, String>> ping();

  @GetMapping("/by-id/{id}")
  ResponseEntity<FileEvent> findById(@PathVariable("id") String id);

  @GetMapping("/download/{id}")
  ResponseEntity<ByteArrayResource> download(@PathVariable("id") String id) throws Exception;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<FileEvent> upload(@RequestPart("file") MultipartFile file,
                                   @RequestParam(value = "fileUploadType",
                                                 defaultValue = "SHARED_FILE") FileEventType fileUploadType) throws Exception;


  @DeleteMapping
  Map.Entry<String, String> delete(@RequestParam("id") String id);
}
