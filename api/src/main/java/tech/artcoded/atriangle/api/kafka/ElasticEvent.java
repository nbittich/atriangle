package tech.artcoded.atriangle.api.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElasticEvent implements KafkaEvent {
  private String index;
  private String settings = "{}";
  private String mappings = "{}";
  private String id;
  private EventType eventType;
  private String json;
  private boolean createIndex;
}
