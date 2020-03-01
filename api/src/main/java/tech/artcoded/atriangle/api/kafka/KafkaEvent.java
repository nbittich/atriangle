package tech.artcoded.atriangle.api.kafka;

public interface KafkaEvent {
  public enum EventType {
    ELASTIC_SINK,
    RDF_SINK,
    FILE_SINK
  }

  String getId();

  String getJson();

  EventType getEventType();
}
