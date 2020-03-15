package tech.artcoded.atriangle.api.kafka;

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
  private boolean createIndex;
  private String elasticMappingsJson;
  private String elasticSettingsJson;
  private String namespace;
  private String shaclModel;
}
