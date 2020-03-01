package tech.artcoded.atriangle.api.kafka;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaEvent implements IKafkaEvent{
  private String id;
  private String json;
  private EventType eventType;
  private String event;
}
