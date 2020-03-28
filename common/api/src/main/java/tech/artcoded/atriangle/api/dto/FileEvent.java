package tech.artcoded.atriangle.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FileEvent {
  private String id;
  private String contentType;
  private FileEventType eventType;
  private String originalFilename;
  private String name;
  @JsonIgnore
  private String pathToFile;
  private long size;

  @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss",
              timezone = "Europe/Brussels")
  @Builder.Default
  private Date creationDate = new Date();

  @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss",
              timezone = "Europe/Brussels")
  private Date lastModifiedDate;
}
