package tech.artcoded.atriangle.api.kafka;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KafkaEvent {
  public enum EventType {
    ELASTIC_SINK,
    RDF_SINK,
    FILE_SINK
  }

  private String id;
  private String json;
  private EventType eventType;
}
