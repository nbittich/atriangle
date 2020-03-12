package tech.artcoded.atriangle.api.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.artcoded.atriangle.api.IdGenerators;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEvent {
  @Builder.Default
  private String id = IdGenerators.UUID_SUPPLIER.get();
  private String name;
  @Builder.Default
  private List<FileEvent> fileEvents = List.of();
}