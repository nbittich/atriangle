package tech.artcoded.atriangle.rest.upload;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.kafka.FileEvent;
import tech.artcoded.atriangle.api.kafka.FileEventType;
import tech.artcoded.atriangle.core.kafka.LoggerAction;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.core.rest.util.RestUtil;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

@CrossOriginRestController
@ApiOperation("File Upload")
@Slf4j
public class FileUploadController implements PingControllerTrait {
  private final FileUploadService uploadService;
  private final LoggerAction loggerAction;

  @Inject
  public FileUploadController(FileUploadService uploadService,
                              LoggerAction loggerAction) {
    this.uploadService = uploadService;
    this.loggerAction = loggerAction;
  }

  @GetMapping("/by-id")
  public ResponseEntity<FileEvent> findById(@RequestParam("id") String id) {
    return uploadService.findOneById(id)
                        .map(FileUpload::transform)
                        .map(ResponseEntity.ok()::body)
                        .orElseGet(ResponseEntity.notFound()::build);
  }

  @GetMapping("/download")
  public ResponseEntity<ByteArrayResource> download(@RequestParam("id") String id) throws Exception {
    Optional<FileUpload> upload = uploadService.findById(id);
    return upload.map(FileUpload::transform)
                 .stream()
                 .peek(event -> loggerAction.info(event::getId, "Download request: %s, name: %s, content-type: %s, event type: %s ", event
                   .getId(), event.getName(), event.getContentType(), event.getEventType()))
                 .map(event -> RestUtil.transformToByteArrayResource(event, uploadService.uploadToByteArray(event)))
                 .findFirst()
                 .orElseGet(ResponseEntity.notFound()::build);
  }


  @PostMapping
  public ResponseEntity<FileEvent> upload(@RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "fileUploadType",
                                                        defaultValue = "SHARED_FILE") FileEventType fileUploadType) throws Exception {
    return Optional.of(uploadService.upload(file, fileUploadType))
                   .stream()
                   .peek(event -> loggerAction.info(event::getId, "Upload request: %s, name: %s, content-type: %s, event type: %s ", event
                     .getId(), event.getName(), event.getContentType(), event.getEventType()))
                   .map(ResponseEntity::ok)
                   .findFirst()
                   .orElseGet(ResponseEntity.notFound()::build)
      ;
  }


  @DeleteMapping
  public Map.Entry<String, String> delete(@RequestParam("id") String id) {
    FileUpload byId = uploadService.findById(id)
                                   .stream()
                                   .peek(upload -> loggerAction.info(upload::getId, "Delete request: %s, name: %s", upload.getId(), upload
                                     .getName()))
                                   .findFirst()
                                   .orElseThrow(() -> new RuntimeException("Upload not found on disk"));
    uploadService.deleteOnDisk(byId);
    return Map.entry("message", id + "file will be deleted");
  }
}
