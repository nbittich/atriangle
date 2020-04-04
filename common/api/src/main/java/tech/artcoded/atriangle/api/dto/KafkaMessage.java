package tech.artcoded.atriangle.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMessage<K, V> {
  private String outTopic;
  private K key;
  private V value;
}
