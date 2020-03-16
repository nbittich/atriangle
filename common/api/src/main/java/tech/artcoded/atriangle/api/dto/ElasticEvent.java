package tech.artcoded.atriangle.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElasticEvent {
  private String index;
  private FileEvent settings;
  private FileEvent mappings;

  private boolean createIndex;
}
