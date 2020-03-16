package tech.artcoded.atriangle.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SinkRequest {
  private String projectId;
  private boolean sinkToElastic;
  private String namespace;
  private String elasticSettingsFileEventId;
  private String shaclFileEventId;
  private String elasticMappingsFileEventId;
  private String rdfFileEventId;
}
