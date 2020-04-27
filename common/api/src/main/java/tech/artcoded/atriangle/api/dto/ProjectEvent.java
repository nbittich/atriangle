package tech.artcoded.atriangle.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.artcoded.atriangle.api.IdGenerators;

import java.util.Date;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEvent {
  @Builder.Default
  private String id = IdGenerators.UUID_SUPPLIER.get();
  private String name;
  private String description;

  @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss",
              timezone = "Europe/Brussels")
  @Builder.Default
  protected Date creationDate = new Date();

  @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss",
              timezone = "Europe/Brussels")
  protected Date lastModifiedDate;

  @Builder.Default
  private List<FileEvent> fileEvents = List.of();

  @Builder.Default
  private List<SparqlQueryRequest> sparqlQueries = List.of();
}
