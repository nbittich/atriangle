package tech.artcoded.atriangle.rest.project;

import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.dto.*;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.controller.BuildInfoControllerTrait;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.feign.clients.project.ProjectRestFeignClient;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@CrossOriginRestController
@ApiOperation("Project Rest")
@Slf4j
public class ProjectRestController implements PingControllerTrait, BuildInfoControllerTrait, ProjectRestFeignClient {
  private final ProjectRestService projectRestService;
  private final ProjectSinkProducer projectSinkProducer;

  @Getter
  private final BuildProperties buildProperties;

  @Inject
  public ProjectRestController(ProjectRestService projectRestService,
                               ProjectSinkProducer projectSinkProducer,
                               BuildProperties buildProperties) {
    this.projectRestService = projectRestService;
    this.projectSinkProducer = projectSinkProducer;
    this.buildProperties = buildProperties;
  }

  @Override
  public ResponseEntity<ProjectEvent> createProject(String name) {
    return ResponseEntity.ok(projectRestService.newProject(name));
  }

  @Override
  public ResponseEntity<ProjectEvent> addFile(MultipartFile multipartFile,
                                              String projectId) {
    return projectRestService.addFile(projectId, multipartFile)
                             .map(ResponseEntity::ok)
                             .orElseGet(ResponseEntity.badRequest()::build);
  }

  @Override
  public ResponseEntity<ProjectEvent> findByName(String name) {
    return projectRestService.findByName(name)
                             .map(ResponseEntity::ok)
                             .orElseGet(ResponseEntity.notFound()::build);
  }

  @Override
  public ResponseEntity<List<LogEvent>> getLogsForProject(String projectId) {
    return projectRestService.getLogsForProject(projectId)
                             .map(ResponseEntity::ok)
                             .orElseGet(ResponseEntity.notFound()::build);
  }

  @Override
  public ResponseEntity<ByteArrayResource> downloadFile(String projectId,
                                                        String fileId) {
    return projectRestService.downloadFile(projectId, fileId);
  }

  @Override
  public ResponseEntity<String> shaclValidation(String projectId, String shapesFileId, String rdfModelFileId) {
    return projectRestService.shaclValidation(projectId, shapesFileId, rdfModelFileId);
  }

  @Override
  public void deleteFile(String projectId, String fileId) {
    CompletableFuture.runAsync(() -> projectRestService.deleteFile(projectId, fileId));
  }

  @Override
  public void deleteByName(String name) {
    projectRestService.deleteByName(name);
  }

  @Override
  public void deleteById(String id) {
    projectRestService.deleteById(id);
  }

  @Override
  public ResponseEntity<ProjectEvent> findById(String id) {
    return projectRestService.findById(id)
                             .map(ResponseEntity::ok)
                             .orElseGet(ResponseEntity.notFound()::build);
  }

  @Override
  public List<ProjectEvent> findAll() {
    return projectRestService.findAll();
  }

  @Override
  public ResponseEntity<ProjectEvent> skosConversion(String projectId, boolean labelSkosXl,
                                                     boolean ignorePostTreatmentsSkos, String xlsFileEventId) {
    FileEvent xlsFileEvent = projectRestService.getFileMetadata(projectId, xlsFileEventId)
                                               .orElseThrow(() -> new RuntimeException("file  not found"));
    if (!CommonConstants.XLSX_MEDIA_TYPE.equals(xlsFileEvent.getContentType())) {
      log.error("only xlsx type supported, provided {}", xlsFileEvent.getContentType());
      return ResponseEntity.badRequest()
                           .build();
    }
    return projectRestService.skosConversion(projectId, labelSkosXl, ignorePostTreatmentsSkos, xlsFileEvent)
                             .map(ResponseEntity::ok)
                             .orElseGet(ResponseEntity.badRequest()::build);
  }

  @Override
  public ResponseEntity<Void> sink(SinkRequest sinkRequest) {
    projectSinkProducer.sink(sinkRequest);
    return ResponseEntity.accepted()
                         .build();
  }

}
