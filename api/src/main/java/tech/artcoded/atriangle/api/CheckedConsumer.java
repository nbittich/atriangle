package tech.artcoded.atriangle.api;

import lombok.SneakyThrows;

@FunctionalInterface
public interface CheckedConsumer<T> {
  void consume(T value) throws Exception;

  @SneakyThrows
  default void safeConsume(T value) {
    consume(value);
  }
}
