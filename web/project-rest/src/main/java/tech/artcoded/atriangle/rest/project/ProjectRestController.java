package tech.artcoded.atriangle.rest.project;

import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.kafka.ProjectEvent;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.core.rest.util.ATriangleByteArrayMultipartFile;

import javax.inject.Inject;
import java.util.List;

@CrossOriginRestController
@ApiOperation("Project Rest")
@Slf4j
public class ProjectRestController implements PingControllerTrait {
  private final ProjectRestService projectRestService;

  @Inject
  public ProjectRestController(ProjectRestService projectRestService) {
    this.projectRestService = projectRestService;
  }

  @PostMapping
  public ResponseEntity<ProjectEvent> createProject(@RequestParam("name") String name) {
    return ResponseEntity.ok(projectRestService.newProject(name));
  }

  @PutMapping(path = "/add-file",
              consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @SneakyThrows
  public ResponseEntity<ProjectEvent> addFile(@RequestParam("file") MultipartFile multipartFile,
                                              @RequestParam("id") String projectId) {


    ATriangleByteArrayMultipartFile copyMultipart = ATriangleByteArrayMultipartFile
      .builder()
      .bytes(multipartFile.getBytes())
      .contentType(multipartFile.getContentType())
      .name(multipartFile.getName())
      .originalFilename(multipartFile.getOriginalFilename())
      .build();
    return projectRestService.addFile(projectId, copyMultipart).map(ResponseEntity::ok)
                             .orElseGet(ResponseEntity.badRequest()::build);
  }

  @GetMapping("/by-name/{name}")
  public ResponseEntity<ProjectEvent> findByName(@PathVariable("name") String name) {
    return projectRestService.findByName(name).map(ResponseEntity::ok)
                             .orElseGet(ResponseEntity.notFound()::build);
  }

  @DeleteMapping("/by-name/{name}")
  public void deleteByName(@PathVariable("name") String name) {
    projectRestService.deleteByName(name);
  }

  @DeleteMapping("/by-id/{id}")
  public void deleteById(@PathVariable("id") String id) {
    projectRestService.deleteById(id);
  }

  @GetMapping("/by-id/{id}")
  public ResponseEntity<ProjectEvent> findById(@PathVariable("id") String id) {
    return projectRestService.findById(id).map(ResponseEntity::ok)
                             .orElseGet(ResponseEntity.notFound()::build);
  }

  @GetMapping("/list")
  public List<ProjectEvent> findAll() {
    return projectRestService.findAll();
  }

}
