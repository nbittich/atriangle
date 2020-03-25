package tech.artcoded.atriangle.api.dto;

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
  private byte[] response;
  private Date finishedDate;
  private SinkResponseStatus sinkResponsestatus;

  public String responseAsString() {
    return new String(response);
  }
}
