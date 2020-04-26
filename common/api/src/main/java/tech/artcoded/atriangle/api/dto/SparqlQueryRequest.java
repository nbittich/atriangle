package tech.artcoded.atriangle.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SparqlQueryRequest {
  public enum SparqlQueryRequestType {
    ASK_QUERY, SELECT_QUERY, CONSTRUCT_QUERY
  }

  private String projectId;
  private String freemarkerTemplateFileId;
  private Map<String, String> variables;
  private SparqlQueryRequestType type;
}
