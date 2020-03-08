package tech.artcoded.atriangle.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

@CrossOriginRestController
@RequestMapping("/upload")
@Slf4j
public class FileUploadController {
  private final FileUploadService uploadService;

  @Inject
  public FileUploadController(FileUploadService uploadService) {
    this.uploadService = uploadService;
  }

  @GetMapping
  public Page<FileUpload> paginatedEntries(Pageable pageable) {
    return uploadService.findAll(pageable);
  }

  @GetMapping("/by-type")
  public Page<FileUpload> findAllByType(@RequestParam("type") FileUploadType uploadType, Pageable pageable) {
    return uploadService.findAllByUploadType(uploadType, pageable);
  }

  @GetMapping("/by-author")
  public Page<FileUpload> findAllByCreatedBy(@RequestParam("author") String author, Pageable pageable) {
    return uploadService.findAllByCreatedBy(author, pageable);
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
    return upload.map(this::transformToByteArrayResource)
                 .orElseGet(ResponseEntity.notFound()::build);
  }

  private ResponseEntity<ByteArrayResource> transformToByteArrayResource(FileUpload upload) {
    return Optional.ofNullable(upload)
                   .map(u -> ResponseEntity.ok()
                                           .contentType(MediaType.parseMediaType(u.getContentType()))
                                           .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + u.getOriginalFilename() + "\"")
                                           .body(new ByteArrayResource(uploadService.uploadToByteArray(u))))
                   .orElseGet(ResponseEntity.notFound()::build);
  }

  @PostMapping
  public FileUpload upload(@RequestParam("file") MultipartFile file,
                           @RequestParam(value = "fileUploadType",
                                         defaultValue = "SHARED_FILE") FileUploadType fileUploadType) throws Exception {
    return uploadService.upload(file, fileUploadType);
  }


  @DeleteMapping
  public Map.Entry<String, String> delete(@RequestParam("id") String id) {
    uploadService.deleteOnDisk(uploadService.findById(id)
                                            .orElseThrow(() -> new RuntimeException("Upload not found on disk")));
    return Map.entry("message", id + "file will be deleted");
  }
}
