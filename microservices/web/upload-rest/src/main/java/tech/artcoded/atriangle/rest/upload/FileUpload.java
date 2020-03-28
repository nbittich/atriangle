package tech.artcoded.atriangle.rest.upload;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.dto.FileEvent;
import tech.artcoded.atriangle.api.dto.FileEventType;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUpload {

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
    upload.setCreationDate(new Date());
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
    upload.setCreationDate(new Date());

    return upload;
  }

  @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss",
              timezone = "Europe/Brussels")
  private Date creationDate;

  @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss",
              timezone = "Europe/Brussels")
  private Date lastModifiedDate;

  @Id
  private String id;

  private String contentType;
  private FileEventType uploadType;
  private String originalFilename;
  private String name;
  private String pathToFile;
  private long size;


}
