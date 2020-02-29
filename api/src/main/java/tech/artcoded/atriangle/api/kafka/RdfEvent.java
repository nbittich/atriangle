package tech.artcoded.atriangle.api.kafka;

import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RdfEvent extends KafkaEvent {
  private String graphUri;
}
