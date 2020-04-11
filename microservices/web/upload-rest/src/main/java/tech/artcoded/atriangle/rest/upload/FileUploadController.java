package tech.artcoded.atriangle.rest.upload;

import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.dto.FileEvent;
import tech.artcoded.atriangle.api.dto.FileEventType;
import tech.artcoded.atriangle.core.kafka.LoggerAction;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.annotation.SwaggerHeaderAuthentication;
import tech.artcoded.atriangle.core.rest.controller.BuildInfoControllerTrait;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.core.rest.util.RestUtil;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@CrossOriginRestController
@ApiOperation("File Upload")
@Slf4j
public class FileUploadController implements BuildInfoControllerTrait, PingControllerTrait, FileRestFeignClient {
  private final FileUploadService uploadService;
  private final LoggerAction loggerAction;

  @Getter
  private final BuildProperties buildProperties;

  @Inject
  public FileUploadController(FileUploadService uploadService,
                              LoggerAction loggerAction, BuildProperties buildProperties) {
    this.uploadService = uploadService;
    this.loggerAction = loggerAction;
    this.buildProperties = buildProperties;
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<FileEvent> findById(String id) {
    return uploadService.findOneById(id)
                        .map(FileUpload::transform)
                        .map(ResponseEntity.ok()::body)
                        .orElseGet(ResponseEntity.notFound()::build);
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<ByteArrayResource> download(String id, String correlationId) throws Exception {
    Optional<FileUpload> upload = uploadService.findOneById(id);
    return upload.map(FileUpload::transform)
                 .stream()
                 .peek(event -> loggerAction.info(() -> correlationId, "Download request: %s, name: %s, content-type: %s, event type: %s ", event
                   .getId(), event.getName(), event.getContentType(), event.getEventType()))
                 .map(event -> RestUtil.transformToByteArrayResource(event, uploadService.uploadToByteArray(event)))
                 .findFirst()
                 .orElseGet(ResponseEntity.notFound()::build);
  }


  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<FileEvent> upload(MultipartFile file, FileEventType fileUploadType,
                                          String correlationId) throws Exception {
    return Optional.of(uploadService.upload(file, fileUploadType))
                   .stream()
                   .peek(event -> loggerAction.info(() -> correlationId, "Upload request: %s, name: %s, content-type: %s, event type: %s ", event
                     .getId(), event.getName(), event.getContentType(), event.getEventType()))
                   .map(ResponseEntity::ok)
                   .findFirst()
                   .orElseGet(ResponseEntity.badRequest()::build)
      ;
  }


  @Override
  @SwaggerHeaderAuthentication
  public Map.Entry<String, String> delete(String id) {
    FileUpload byId = uploadService.findOneById(id)
                                   .stream()
                                   .peek(upload -> loggerAction.info(upload::getId, "Delete request: %s, name: %s", upload.getId(), upload
                                     .getName()))
                                   .findFirst()
                                   .orElseThrow(() -> new RuntimeException("Upload not found on disk"));
    CompletableFuture.runAsync(() -> uploadService.deleteOnDisk(byId));
    return Map.entry("message", id + "file will be deleted");
  }
}
