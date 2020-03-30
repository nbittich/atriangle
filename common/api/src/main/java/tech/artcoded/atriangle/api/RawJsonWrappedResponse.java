package tech.artcoded.atriangle.api;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class RawJsonWrappedResponse {
  @Builder.Default
  private final String id = UUID.randomUUID().toString();
  @JsonRawValue
  private String data;
}
