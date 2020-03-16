package tech.artcoded.atriangle.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestEvent {
  private String elasticIndex;
  private boolean sinkToElastic;
  private FileEvent elasticMappingsJson;
  private FileEvent elasticSettingsJson;
  private String namespace;
}
