package tech.artcoded.atriangle.feign.clients.project;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.dto.*;

import java.util.List;

import static tech.artcoded.atriangle.api.dto.SparqlQueryRequest.SparqlQueryRequestType;

public interface ProjectRestFeignClient {

  @PostMapping
  ResponseEntity<ProjectEvent> createProject(
      @RequestParam("name") String name,
      @RequestParam(value = "description", defaultValue = "N/A") String description);

  @PostMapping("/{projectId}/update-description")
  ResponseEntity<ProjectEvent> updateProjectDescription(
      @PathVariable("projectId") String projectId,
      @RequestParam(value = "description") String description);

  @GetMapping("/list")
  List<ProjectEvent> findAll();

  @GetMapping("/by-name/{name}")
  ResponseEntity<ProjectEvent> findByName(@PathVariable("name") String name);

  @GetMapping("/by-id/{projectId}")
  ResponseEntity<ProjectEvent> findById(@PathVariable("projectId") String projectId);

  @PostMapping(path = "/add-raw-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<ProjectEvent> addRawFile(
      @RequestPart("file") MultipartFile file, @RequestParam("projectId") String projectId);

  @PostMapping(path = "/add-rdf-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<ProjectEvent> addRdfFile(
      @RequestPart("file") MultipartFile file, @RequestParam("projectId") String projectId);

  @PostMapping(path = "/add-shacl-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<ProjectEvent> addShaclFile(
      @RequestPart("file") MultipartFile file, @RequestParam("projectId") String projectId);

  @PostMapping(path = "/add-sparql-query-template", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<ProjectEvent> addFreemarkerSparqlTemplate(
      @RequestPart("file") MultipartFile file,
      @RequestParam("projectId") String projectId,
      @RequestParam("queryType") SparqlQueryRequestType queryType);

  @PostMapping(path = "/add-skos-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<ProjectEvent> addSkosFile(
      @RequestPart("file") MultipartFile file, @RequestParam("projectId") String projectId);

  @PostMapping(
      path = "/execute-sparql-query",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<SparqlQueryResponse> executeSparqlQuery(
      @RequestBody SparqlQueryRequest queryRequest);

  @GetMapping("/{projectId}/logs")
  ResponseEntity<List<LogEvent>> getLogsForProject(@PathVariable("projectId") String projectId);

  @GetMapping("/{projectId}/download-file/{fileId}")
  ResponseEntity<ByteArrayResource> downloadFile(
      @PathVariable("projectId") String projectId, @PathVariable("fileId") String fileId);

  @DeleteMapping("/{projectId}/delete-file/{fileId}")
  void deleteFile(
      @PathVariable("projectId") String projectId, @PathVariable("fileId") String fileId);

  @DeleteMapping("/by-name/{name}")
  void deleteByName(@PathVariable("name") String name);

  @DeleteMapping("/by-id/{projectId}")
  void deleteById(@PathVariable("projectId") String projectId);

  @GetMapping("/{projectId}/shacl-validation")
  ResponseEntity<String> shaclValidation(
      @PathVariable("projectId") String projectId,
      @RequestParam("shapesFileId") String shapesFileId,
      @RequestParam("rdfModelFileId") String rdfModelFileId);

  @PostMapping("/conversion/skos")
  ResponseEntity<ProjectEvent> skosConversion(
      @RequestParam("projectId") String projectId,
      @RequestParam(value = "labelSkosXl", required = false) boolean labelSkosXl,
      @RequestParam(value = "ignorePostTreatmentsSkos", required = false)
          boolean ignorePostTreatmentsSkos,
      @RequestParam("xlsFileEventId") String xlsFileEventId);

  @PostMapping("/sink")
  ResponseEntity<Void> sink(@RequestBody SinkRequest sinkRequest);
}
