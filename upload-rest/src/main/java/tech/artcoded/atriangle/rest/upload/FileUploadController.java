package tech.artcoded.atriangle.rest.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.kafka.FileEvent;
import tech.artcoded.atriangle.api.kafka.FileEventType;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

@CrossOriginRestController
@Slf4j
public class FileUploadController implements PingControllerTrait {
  private final FileUploadService uploadService;

  @Inject
  public FileUploadController(FileUploadService uploadService) {
    this.uploadService = uploadService;
  }

  @GetMapping("/by-id")
  public ResponseEntity<FileUpload> findById(@RequestParam("id") String id) {
    return uploadService.findOneById(id)
                        .map(ResponseEntity.ok()::body)
                        .orElseGet(ResponseEntity.notFound()::build);
  }

  @GetMapping("/download")
  public ResponseEntity<ByteArrayResource> download(@RequestParam("id") String id) throws Exception {
    Optional<FileUpload> upload = uploadService.findById(id);
    return upload.map(FileUpload::transform)
                 .map(this::transformToByteArrayResource)
                 .orElseGet(ResponseEntity.notFound()::build);
  }

  private ResponseEntity<ByteArrayResource> transformToByteArrayResource(FileEvent event) {
    return Optional.ofNullable(event)
                   .map(u -> ResponseEntity.ok()
                                           .contentType(MediaType.parseMediaType(u.getContentType()))
                                           .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + u.getOriginalFilename() + "\"")
                                           .body(new ByteArrayResource(uploadService.uploadToByteArray(u))))
                   .orElseGet(ResponseEntity.notFound()::build);
  }

  @PostMapping
  public FileEvent upload(@RequestParam("file") MultipartFile file,
                          @RequestParam(value = "fileUploadType",
                                        defaultValue = "SHARED_FILE") FileEventType fileUploadType) throws Exception {
    return uploadService.upload(file, fileUploadType);
  }


  @DeleteMapping
  public Map.Entry<String, String> delete(@RequestParam("id") String id) {
    uploadService.deleteOnDisk(uploadService.findById(id)
                                            .orElseThrow(() -> new RuntimeException("Upload not found on disk")));
    return Map.entry("message", id + "file will be deleted");
  }
}
