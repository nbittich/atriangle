package tech.artcoded.atriangle.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class KafkaEventMetadata {
  private String version;
  private String artifactId;
  private String groupId;
  private String moduleName;
  private long offset;
  private int partition;
  private Map<String, String> headers;
}
