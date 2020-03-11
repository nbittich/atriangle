package tech.artcoded.atriangle.api;

import lombok.SneakyThrows;

import java.util.function.Consumer;

@FunctionalInterface
public interface CheckedConsumer<T> {
  static <F> Consumer<F> toConsumer(CheckedConsumer<F> hack) {
    return hack::safeConsume;
  }

  void consume(T value) throws Exception;

  @SneakyThrows
  default void safeConsume(T value) {
    consume(value);
  }
}
