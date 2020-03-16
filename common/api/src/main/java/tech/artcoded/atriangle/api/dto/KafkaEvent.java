package tech.artcoded.atriangle.api.dto;

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
public class KafkaEvent implements IKafkaEvent {
  private String id;
  private String correlationId;
  private EventType eventType;
  private FileEvent inputToSink;
  private FileEvent shaclModel;
  @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss",
              timezone = "Europe/Brussels")
  @Builder.Default
  private Date creationDate = new Date();
  private String event;
}
