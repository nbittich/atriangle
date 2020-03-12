package tech.artcoded.atriangle.rest.project;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tech.artcoded.atriangle.api.kafka.ProjectEvent;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;

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

  @GetMapping("/list")
  public List<ProjectEvent> findAll() {
    return projectRestService.findAll();
  }

}
