package tech.artcoded.atriangle.rest.project;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.CheckedFunction;
import tech.artcoded.atriangle.api.CheckedSupplier;
import tech.artcoded.atriangle.api.dto.FileEvent;
import tech.artcoded.atriangle.api.dto.FileEventType;
import tech.artcoded.atriangle.api.dto.ProjectEvent;
import tech.artcoded.atriangle.core.kafka.LoggerAction;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;
import tech.artcoded.atriangle.feign.clients.sparql.SparqlRestFeignClient;

import javax.inject.Inject;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class ProjectFileService {

  private final LoggerAction loggerAction;
  private final FileRestFeignClient fileRestFeignClient;
  private final SparqlRestFeignClient sparqlRestFeignClient;
  private final ProjectService projectService;

  @Inject
  public ProjectFileService(LoggerAction loggerAction,
                            FileRestFeignClient fileRestFeignClient,
                            SparqlRestFeignClient sparqlRestFeignClient,
                            ProjectService projectService) {
    this.loggerAction = loggerAction;
    this.fileRestFeignClient = fileRestFeignClient;
    this.sparqlRestFeignClient = sparqlRestFeignClient;
    this.projectService = projectService;
  }

  public ResponseEntity<ByteArrayResource> downloadFile(String projectId, String fileEventId) {
    try {
      Optional<ProjectEvent> projectEvent = projectService.findById(projectId);
      return projectEvent.flatMap(p -> p.getFileEvents()
                                        .stream()
                                        .filter(file -> file.getId()
                                                            .equals(fileEventId))
                                        .findFirst())
                         .map(CheckedFunction.toFunction(f -> fileRestFeignClient.download(f.getId(), projectId)))
                         .orElseGet(ResponseEntity.notFound()::build);
    }
    catch (Exception e) {
      log.error("error", e);
      loggerAction.error(() -> projectId, "an error occured %s", e.getMessage());
    }
    return ResponseEntity.notFound()
                         .build();
  }

  public Optional<FileEvent> getFileMetadata(String projectId, String fileEventId) {
    Optional<ProjectEvent> projectEvent = projectService.findById(projectId);
    return projectEvent.flatMap(p -> p.getFileEvents()
                                      .stream()
                                      .filter(file -> file.getId()
                                                          .equals(fileEventId))
                                      .findFirst());
  }

  @Transactional
  public Optional<ProjectEvent> addFile(String projectId, MultipartFile file, FileEventType fileEventType) {
    if (FileEventType.RDF_FILE.equals(fileEventType) || FileEventType.SHACL_FILE.equals(fileEventType)) {
      ResponseEntity<Boolean> isRDFResponse = sparqlRestFeignClient.checkFileFormat(file.getOriginalFilename());
      if (!Optional.ofNullable(isRDFResponse)
                   .map(ResponseEntity::getBody)
                   .orElse(false)) {
        throw new RuntimeException("the file is not an rdf file");
      }
    }
    return projectService.findById(projectId)
                         .stream()
                         .map(projectEvent -> {
                           ResponseEntity<FileEvent> fileEvent = CheckedSupplier.toSupplier(() -> fileRestFeignClient.upload(file, fileEventType, projectId))
                                                                                .get();
                           if (!HttpStatus.OK.equals(fileEvent.getStatusCode()) || !fileEvent.hasBody()) {
                             throw new RuntimeException("upload failed with status " + fileEvent.getStatusCode());
                           }

                           loggerAction.info(projectEvent::getId, "new file %s added to project %s", fileEvent.getBody()
                                                                                                              .getName(), projectEvent.getName());

                           return projectEvent.toBuilder()
                                              .lastModifiedDate(new Date())
                                              .fileEvents(Stream.concat(projectEvent.getFileEvents()
                                                                                    .stream(), Stream.of(fileEvent.getBody()))
                                                                .collect(toList()))
                                              .build();
                         })
                         .map(projectService::save)
                         .findFirst();
  }


  @Transactional
  public void deleteFile(String projectId, String fileEventId) {
    Optional<ProjectEvent> projectEvent = projectService.findById(projectId);
    projectEvent.ifPresent((p) -> {
      Optional<FileEvent> fileEvent = p.getFileEvents()
                                       .stream()
                                       .filter(file -> file.getId()
                                                           .equals(fileEventId))
                                       .findAny();
      fileEvent.ifPresent(f -> {
        loggerAction.info(p::getId, " file %s removed from project %s", f.getId(), p.getName());
        fileRestFeignClient.delete(f.getId());
        ProjectEvent newProject = p.toBuilder()
                                   .lastModifiedDate(new Date())
                                   .fileEvents(p.getFileEvents()
                                                .stream()
                                                .filter(file -> !file.getId()
                                                                     .equals(fileEventId))
                                                .collect(toList()))
                                   .build();
        projectService.save(newProject);
      });
    });
  }
}
