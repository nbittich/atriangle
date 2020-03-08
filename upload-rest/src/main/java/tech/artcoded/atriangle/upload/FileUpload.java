package tech.artcoded.atriangle.upload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.core.database.Auditable;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "file_rest_upload")
public class FileUpload extends Auditable<String> {


  public static FileUpload newUpload(MultipartFile file, FileUploadType uploadType, String pathToFile) {
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
                                     FileUploadType uploadType,
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
  private FileUploadType uploadType;
  private String originalFilename;
  private String name;
  @JsonIgnore
  private String pathToFile;
  private long size;

  public FileUploadType getUploadType() {
    return uploadType;
  }

  public void setUploadType(FileUploadType uploadType) {
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
