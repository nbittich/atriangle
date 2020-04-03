package tech.artcoded.atriangle.api;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class RawJsonWrappedResponse {
  @JsonRawValue
  private String data;
}
