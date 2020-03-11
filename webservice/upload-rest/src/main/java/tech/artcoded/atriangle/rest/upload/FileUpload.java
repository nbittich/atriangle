package tech.artcoded.atriangle.rest.upload;

import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.kafka.FileEvent;
import tech.artcoded.atriangle.api.kafka.FileEventType;
import tech.artcoded.atriangle.core.database.Auditable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "file_rest_upload")
public class FileUpload extends Auditable<String> {

  public static FileEvent transform(FileUpload fileUpload) {
    return FileEvent.builder()
                    .creationDate(fileUpload.getCreationDate())
                    .lastModifiedDate(fileUpload.getLastModifiedDate())
                    .id(fileUpload.getId())
                    .contentType(fileUpload.getContentType())
                    .eventType(fileUpload.getUploadType())
                    .name(fileUpload.getName())
                    .originalFilename(fileUpload.getOriginalFilename())
                    .pathToFile(fileUpload.getPathToFile())
                    .size(fileUpload.getSize())
                    .build();
  }

  public static FileUpload newUpload(MultipartFile file, FileEventType uploadType, String pathToFile) {
    FileUpload upload = new FileUpload();
    upload.setId(UUID.randomUUID()
                     .toString());
    upload.setContentType(file.getContentType());
    upload.setOriginalFilename(file.getOriginalFilename());
    upload.setName(file.getOriginalFilename());
    upload.setSize(file.getSize());
    upload.setUploadType(uploadType);
    upload.setPathToFile(pathToFile);
    return upload;
  }

  public static FileUpload newUpload(String contentType,
                                     String originalFilename,
                                     FileEventType uploadType,
                                     String pathToFile) {
    FileUpload upload = new FileUpload();
    upload.setId(UUID.randomUUID()
                     .toString());
    upload.setContentType(contentType);
    upload.setOriginalFilename(originalFilename);
    upload.setName(originalFilename);
    upload.setUploadType(uploadType);
    upload.setPathToFile(pathToFile);
    return upload;
  }

  @Id
  @Column(name = "file_upload_id")
  private String id;

  private String contentType;
  @Enumerated(EnumType.ORDINAL)
  private FileEventType uploadType;
  private String originalFilename;
  private String name;
  private String pathToFile;
  private long size;

  public FileEventType getUploadType() {
    return uploadType;
  }

  public void setUploadType(FileEventType uploadType) {
    this.uploadType = uploadType;
  }

  public String getPathToFile() {
    return pathToFile;
  }

  public void setPathToFile(String pathToFile) {
    this.pathToFile = pathToFile;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOriginalFilename() {
    return originalFilename;
  }

  public void setOriginalFilename(String originalFilename) {
    this.originalFilename = originalFilename;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

}
