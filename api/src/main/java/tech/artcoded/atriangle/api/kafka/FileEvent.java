package tech.artcoded.atriangle.api.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEvent {
  private String id;
  private String contentType;
  private FileEventType eventType;
  private String originalFilename;
  private String name;
  private String pathToFile;
  private long size;

  @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Europe/Brussels")
  private Date creationDate;

  @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Europe/Brussels")
  private Date lastModifiedDate;
}