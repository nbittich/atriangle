package tech.artcoded.atriangle.api.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RdfEvent implements KafkaEvent {
  private String graphUri;
  private String id;
  private final EventType eventType = EventType.RDF_SINK;
  private String json;
}
