package tech.artcoded.atriangle.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.function.Supplier;

@Data
@Builder(toBuilder = true)
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

  private String version;
  private String artifactId;
  private String groupId;
  private String moduleName;

  public static KafkaEventBuilder withBuildPropertiesBuilder(
    Supplier<String> moduleName, Supplier<String> groupId, Supplier<String> artifactId, Supplier<String> version) {
    return KafkaEvent.builder()
                     .artifactId(artifactId.get())
                     .version(version.get())
                     .groupId(groupId.get())
                     .moduleName(moduleName.get());
  }
}
