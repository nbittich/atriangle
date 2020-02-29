package tech.artcoded.atriangle.api.kafka;

import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElasticEvent extends KafkaEvent {
  private String index;
  private String settings = "{}";
  private String mappings = "{}";
  private boolean createIndex;
}
