package tech.artcoded.atriangle.rest.project;

import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.CommonConstants;
import tech.artcoded.atriangle.api.FileHelper;
import tech.artcoded.atriangle.api.dto.*;
import tech.artcoded.atriangle.core.rest.annotation.SwaggerHeaderAuthentication;
import tech.artcoded.atriangle.core.rest.controller.BuildInfoControllerTrait;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.feign.clients.project.ProjectRestFeignClient;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@ApiOperation("Project Rest")
@Slf4j
public class ProjectRestController implements PingControllerTrait, BuildInfoControllerTrait, ProjectRestFeignClient {
  private final ProjectRdfService projectRdfService;
  private final ProjectService projectService;
  private final ProjectFileService projectFileService;
  private final ProjectSinkProducer projectSinkProducer;

  @Getter
  private final BuildProperties buildProperties;

  @Inject
  public ProjectRestController(ProjectRdfService projectRdfService,
                               ProjectService projectService,
                               ProjectFileService projectFileService,
                               ProjectSinkProducer projectSinkProducer,
                               BuildProperties buildProperties) {
    this.projectRdfService = projectRdfService;
    this.projectService = projectService;
    this.projectFileService = projectFileService;
    this.projectSinkProducer = projectSinkProducer;
    this.buildProperties = buildProperties;
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<ProjectEvent> createProject(String name, String description) {
    return ResponseEntity.ok(projectService.newProject(name, description));
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<ProjectEvent> addRawFile(MultipartFile multipartFile,
                                                 String projectId) {
    return projectFileService.addFile(projectId, multipartFile, FileEventType.PROJECT_FILE)
                             .map(ResponseEntity::ok)
                             .orElseGet(ResponseEntity.badRequest()::build);
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<ProjectEvent> addRdfFile(MultipartFile multipartFile,
                                                 String projectId) {
    return projectFileService.addFile(projectId, multipartFile, FileEventType.RDF_FILE)
                             .map(ResponseEntity::ok)
                             .orElseGet(ResponseEntity.badRequest()::build);
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<ProjectEvent> addShaclFile(MultipartFile multipartFile,
                                                   String projectId) {
    return projectFileService.addFile(projectId, multipartFile, FileEventType.SHACL_FILE)
                             .map(ResponseEntity::ok)
                             .orElseGet(ResponseEntity.badRequest()::build);
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<ProjectEvent> addFreemarkerSparqlTemplate(MultipartFile multipartFile,
                                                                  String projectId) {

    String extension = FileHelper.getExtension(multipartFile.getOriginalFilename())
                                 .orElse("N/A");
    if (!extension.equals("ftl")) {
      log.info("file extension not valid: {}, file name {}, original file name {}", extension, multipartFile.getName(), multipartFile.getOriginalFilename());
      throw new RuntimeException("the file is not a freemarker template file");
    }

    return projectFileService.addFile(projectId, multipartFile, FileEventType.FREEMARKER_TEMPLATE_FILE)
                             .map(ResponseEntity::ok)
                             .orElseGet(ResponseEntity.badRequest()::build);
  }

  @Override
  public ResponseEntity<ProjectEvent> addSkosFile(MultipartFile file, String projectId) {

    if (!CommonConstants.XLSX_MEDIA_TYPE.equals(file.getContentType())) {
      throw new RuntimeException("only xlsx type supported");
    }

    return projectFileService.addFile(projectId, file, FileEventType.SKOS_FILE)
                             .map(ResponseEntity::ok)
                             .orElseGet(ResponseEntity.badRequest()::build);
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<List<Map<String, String>>> executeSelectSparqlQuery(String projectId,
                                                                            String freemarkerTemplateFileId,
                                                                            Map<String, String> variables) {
    ProjectEvent projectEvent = projectService.findById(projectId)
                                              .orElseThrow();
    String queryTempl = projectRdfService.getCachedQueryTemplate(projectEvent, freemarkerTemplateFileId);
    String query = projectRdfService.compileQuery(queryTempl, variables);
    String cacheKey = DigestUtils.sha1Hex(projectId + query);
    return ResponseEntity.ok(projectRdfService.executeSelectSparqlQuery(projectEvent, query, cacheKey));
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<String> executeConstructSparqlQuery(String projectId, String freemarkerTemplateFileId,
                                                            Map<String, String> variables) {

    ProjectEvent projectEvent = projectService.findById(projectId)
                                              .orElseThrow();
    String queryTempl = projectRdfService.getCachedQueryTemplate(projectEvent, freemarkerTemplateFileId);
    String query = projectRdfService.compileQuery(queryTempl, variables);
    String cacheKey = DigestUtils.sha1Hex(projectId + query);
    return ResponseEntity.ok(projectRdfService.executeConstructSparqlQuery(projectEvent, query, cacheKey));
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<Boolean> executeAskSparqlQuery(String projectId, String freemarkerTemplateFileId,
                                                       Map<String, String> variables) {
    ProjectEvent projectEvent = projectService.findById(projectId)
                                              .orElseThrow();
    String queryTempl = projectRdfService.getCachedQueryTemplate(projectEvent, freemarkerTemplateFileId);
    String query = projectRdfService.compileQuery(queryTempl, variables);
    String cacheKey = DigestUtils.sha1Hex(projectId + query);
    return ResponseEntity.ok(projectRdfService.executeAskSparqlQuery(projectEvent, query, cacheKey));
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<ProjectEvent> findByName(String name) {
    return projectService.findByName(name)
                         .map(ResponseEntity::ok)
                         .orElseGet(ResponseEntity.notFound()::build);
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<List<LogEvent>> getLogsForProject(String projectId) {
    return projectRdfService.getLogsForProject(projectId)
                            .map(ResponseEntity::ok)
                            .orElseGet(ResponseEntity.notFound()::build);
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<ByteArrayResource> downloadFile(String projectId,
                                                        String fileId) {
    return projectFileService.downloadFile(projectId, fileId);
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<String> shaclValidation(String projectId, String shapesFileId, String rdfModelFileId) {
    return projectRdfService.shaclValidation(projectId, shapesFileId, rdfModelFileId);
  }

  @Override
  @SwaggerHeaderAuthentication
  public void deleteFile(String projectId, String fileId) {
    CompletableFuture.runAsync(() -> projectFileService.deleteFile(projectId, fileId));
  }

  @Override
  @SwaggerHeaderAuthentication
  public void deleteByName(String name) {
    projectService.deleteByName(name);
  }

  @Override
  @SwaggerHeaderAuthentication
  public void deleteById(String id) {
    projectService.deleteById(id);
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<ProjectEvent> findById(String id) {
    return projectService.findById(id)
                         .map(ResponseEntity::ok)
                         .orElseGet(ResponseEntity.notFound()::build);
  }

  @Override
  @SwaggerHeaderAuthentication
  public List<ProjectEvent> findAll() {
    return projectService.findAll();
  }

  @Override
  public ResponseEntity<ProjectEvent> updateProjectDescription(String projectId, String description) {
    return projectService.updateDescription(projectId, description)
                         .map(ResponseEntity::ok)
                         .orElseGet(ResponseEntity.badRequest()::build);
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<ProjectEvent> skosConversion(String projectId, boolean labelSkosXl,
                                                     boolean ignorePostTreatmentsSkos, String xlsFileEventId) {
    FileEvent xlsFileEvent = projectFileService.getFileMetadata(projectId, xlsFileEventId)
                                               .orElseThrow(() -> new RuntimeException("file  not found"));
    if (!FileEventType.SKOS_FILE.equals(xlsFileEvent.getEventType()) || !CommonConstants.XLSX_MEDIA_TYPE.equals(xlsFileEvent.getContentType())) {
      log.error("only xlsx type supported, provided {}", xlsFileEvent.getContentType());

      throw new RuntimeException("only xlsx type supported");
    }
    return projectRdfService.skosConversion(projectId, labelSkosXl, ignorePostTreatmentsSkos, xlsFileEvent)
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
