package tech.artcoded.atriangle.core.rest.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.kafka.FileEvent;
import tech.artcoded.atriangle.api.kafka.FileEventType;

import java.util.Map;

public interface FileUploadControllerTrait {
  @GetMapping("/by-id")
  ResponseEntity<FileEvent> findById(@RequestParam("id") String id);

  @GetMapping("/download")
  ResponseEntity<ByteArrayResource> download(@RequestParam("id") String id) throws Exception;

  @PostMapping
  ResponseEntity<FileEvent> upload(@RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "fileUploadType",
                                                 defaultValue = "SHARED_FILE") FileEventType fileUploadType) throws Exception;


  @DeleteMapping
  Map.Entry<String, String> delete(@RequestParam("id") String id);
}
