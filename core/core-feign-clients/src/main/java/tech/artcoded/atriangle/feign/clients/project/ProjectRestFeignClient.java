package tech.artcoded.atriangle.feign.clients.project;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import tech.artcoded.atriangle.api.kafka.ProjectEvent;

import java.io.File;
import java.util.List;

public interface ProjectRestFeignClient {
  @PostMapping
  ResponseEntity<ProjectEvent> createProject(@RequestParam("name") String name);

  @GetMapping("/list")
  List<ProjectEvent> findAll();


  @GetMapping("/by-name/{name}")
  ResponseEntity<ProjectEvent> findByName(@PathVariable("name") String name);

  @GetMapping("/by-id/{id}")
  ResponseEntity<ProjectEvent> findById(@PathVariable("id") String id);


  @PutMapping(path = "/add-file",
              consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<ProjectEvent> addFile(@RequestPart("file") File file,
                                       @RequestParam("id") String projectId);


  @GetMapping("/{projectId}/download/{fileId}")
  ResponseEntity<ByteArrayResource> downloadFile(@PathVariable("projectId") String projectId,
                                                 @PathVariable("fileId") String fileId);

  @DeleteMapping("/{projectId}/delete-file/{fileId}")
  void deleteFile(@PathVariable("projectId") String projectId,
                  @PathVariable("fileId") String fileId);

  @DeleteMapping("/by-name/{name}")
  void deleteByName(@PathVariable("name") String name);

  @DeleteMapping("/by-id/{id}")
  void deleteById(@PathVariable("id") String id);
}
