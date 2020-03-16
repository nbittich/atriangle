package tech.artcoded.atriangle.feign.clients.project;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import tech.artcoded.atriangle.api.dto.ProjectEvent;
import tech.artcoded.atriangle.api.dto.SinkRequest;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface ProjectRestFeignClient {
  @PostMapping
  ResponseEntity<ProjectEvent> createProject(@RequestParam("name") String name);

  @GetMapping("/list")
  List<ProjectEvent> findAll();


  @GetMapping("/by-name/{name}")
  ResponseEntity<ProjectEvent> findByName(@PathVariable("name") String name);

  @GetMapping("/by-id/{projectId}")
  ResponseEntity<ProjectEvent> findById(@PathVariable("projectId") String projectId);


  @PutMapping(path = "/add-file",
              consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<ProjectEvent> addFile(@RequestPart("file") File file,
                                       @RequestParam("projectId") String projectId);


  @GetMapping("/{projectId}/download-file/{fileId}")
  ResponseEntity<ByteArrayResource> downloadFile(@PathVariable("projectId") String projectId,
                                                 @PathVariable("fileId") String fileId);

  @DeleteMapping("/{projectId}/delete-file/{fileId}")
  void deleteFile(@PathVariable("projectId") String projectId,
                  @PathVariable("fileId") String fileId);

  @DeleteMapping("/by-name/{name}")
  void deleteByName(@PathVariable("name") String name);

  @DeleteMapping("/by-id/{projectId}")
  void deleteById(@PathVariable("projectId") String projectId);

  @GetMapping("/{projectId}/shacl-validation")
  ResponseEntity<String> shaclValidation(@PathVariable("projectId") String projectId,
                                         @RequestParam("shapesFileId") String shapesFileId,
                                         @RequestParam("rdfModelFileId") String rdfModelFileId);

  @GetMapping("/ping-skos")
  ResponseEntity<Map<String, String>> pingSkos();

  @PostMapping("/conversion/skos")
  ResponseEntity<ProjectEvent> skosConversion(
    @RequestParam("projectId") String projectId,
    @RequestParam(value = "labelSkosXl",
                  required = false) boolean labelSkosXl,
    @RequestParam(value = "ignorePostTreatmentsSkos",
                  required = false) boolean ignorePostTreatmentsSkos,
    @RequestParam("xlsFileEventId") String xlsFileEventId
  );

  @PostMapping("/{projectId}/sink")
  ResponseEntity<Void> sink(@RequestBody SinkRequest sinkRequest);
}
