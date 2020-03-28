package tech.artcoded.atriangle.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SinkResponse {
  public enum SinkResponseStatus {SUCCESS, ERROR}

  private EventType responseType;
  private String response;
  @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Europe/Brussels")
  private Date finishedDate;
  private SinkResponseStatus sinkResponsestatus;

}
