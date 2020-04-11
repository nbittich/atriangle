package tech.artcoded.atriangle.rest.project;

import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.FileHelper;
import tech.artcoded.atriangle.api.dto.*;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.annotation.SwaggerHeaderAuthentication;
import tech.artcoded.atriangle.core.rest.controller.BuildInfoControllerTrait;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.feign.clients.project.ProjectRestFeignClient;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
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
  @SwaggerHeaderAuthentication
  public ResponseEntity<ProjectEvent> createProject(String name) {
    return ResponseEntity.ok(projectRestService.newProject(name));
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<ProjectEvent> addFile(MultipartFile multipartFile,
                                              String projectId) {
    return projectRestService.addFile(projectId, multipartFile)
                             .map(ResponseEntity::ok)
                             .orElseGet(ResponseEntity.badRequest()::build);
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<ProjectEvent> addFreemarkerSparqlTemplate(MultipartFile multipartFile,
                                                                  String projectId) {

    String extension = FileHelper.getExtension(multipartFile.getOriginalFilename()).orElse("N/A");
    if (!extension.equals("ftl")) {
      log.info("file extension not valid: {}, file name {}, original file name {}", extension, multipartFile.getName(), multipartFile.getOriginalFilename());
      return ResponseEntity.badRequest()
                           .build();
    }

    return projectRestService.addFile(projectId, multipartFile, FileEventType.FREEMARKER_TEMPLATE_FILE)
                             .map(ResponseEntity::ok)
                             .orElseGet(ResponseEntity.badRequest()::build);
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<List<Map<String, String>>> executeSelectSparqlQuery(String projectId,
                                                                            String freemarkerTemplateFileId,
                                                                            Map<String, String> variables) {
    ProjectEvent projectEvent = projectRestService.findById(projectId)
                                                  .orElseThrow();
    String query = projectRestService.compileQuery(projectEvent, freemarkerTemplateFileId, variables);
    String cacheKey = DigestUtils.sha1Hex(query);
    return ResponseEntity.ok(projectRestService.executeSelectSparqlQuery(projectEvent, query, cacheKey));
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<String> executeConstructSparqlQuery(String projectId, String freemarkerTemplateFileId,
                                                            Map<String, String> variables) {

    ProjectEvent projectEvent = projectRestService.findById(projectId)
                                                  .orElseThrow();
    String query = projectRestService.compileQuery(projectEvent, freemarkerTemplateFileId, variables);
    String cacheKey = DigestUtils.sha1Hex(query);
    return ResponseEntity.ok(projectRestService.executeConstructSparqlQuery(projectEvent, query, cacheKey));
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<Boolean> executeAskSparqlQuery(String projectId, String freemarkerTemplateFileId,
                                                       Map<String, String> variables) {
    ProjectEvent projectEvent = projectRestService.findById(projectId)
                                                  .orElseThrow();
    String query = projectRestService.compileQuery(projectEvent, freemarkerTemplateFileId, variables);
    String cacheKey = DigestUtils.sha1Hex(query);
    return ResponseEntity.ok(projectRestService.executeAskSparqlQuery(projectEvent, query, cacheKey));
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<ProjectEvent> findByName(String name) {
    return projectRestService.findByName(name)
                             .map(ResponseEntity::ok)
                             .orElseGet(ResponseEntity.notFound()::build);
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<List<LogEvent>> getLogsForProject(String projectId) {
    return projectRestService.getLogsForProject(projectId)
                             .map(ResponseEntity::ok)
                             .orElseGet(ResponseEntity.notFound()::build);
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<ByteArrayResource> downloadFile(String projectId,
                                                        String fileId) {
    return projectRestService.downloadFile(projectId, fileId);
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<String> shaclValidation(String projectId, String shapesFileId, String rdfModelFileId) {
    return projectRestService.shaclValidation(projectId, shapesFileId, rdfModelFileId);
  }

  @Override
  @SwaggerHeaderAuthentication
  public void deleteFile(String projectId, String fileId) {
    CompletableFuture.runAsync(() -> projectRestService.deleteFile(projectId, fileId));
  }

  @Override
  @SwaggerHeaderAuthentication
  public void deleteByName(String name) {
    projectRestService.deleteByName(name);
  }

  @Override
  @SwaggerHeaderAuthentication
  public void deleteById(String id) {
    projectRestService.deleteById(id);
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<ProjectEvent> findById(String id) {
    return projectRestService.findById(id)
                             .map(ResponseEntity::ok)
                             .orElseGet(ResponseEntity.notFound()::build);
  }

  @Override
  @SwaggerHeaderAuthentication
  public List<ProjectEvent> findAll() {
    return projectRestService.findAll();
  }

  @Override
  @SwaggerHeaderAuthentication
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
  @SwaggerHeaderAuthentication
  public ResponseEntity<Void> sink(SinkRequest sinkRequest) {
    projectSinkProducer.sink(sinkRequest);
    return ResponseEntity.accepted()
                         .build();
  }

}
