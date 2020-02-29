package tech.artcoded.atriangle.api.kafka;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KafkaEvent {
  private String id;
  private String json;
}
