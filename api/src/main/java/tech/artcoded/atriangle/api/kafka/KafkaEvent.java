package tech.artcoded.atriangle.api.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaEvent implements IKafkaEvent {
  private String id;
  private String correlationId;
  private String json;
  private EventType eventType;
  private String event;
}
