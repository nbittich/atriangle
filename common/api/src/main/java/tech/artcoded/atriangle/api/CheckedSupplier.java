package tech.artcoded.atriangle.api;

import lombok.SneakyThrows;

import java.util.function.Supplier;

@FunctionalInterface
public interface CheckedSupplier<T> {
  static <F> Supplier<F> toSupplier(CheckedSupplier<F> hack) {
    return hack::safeGet;
  }

  T get() throws Exception;

  @SneakyThrows
  default T safeGet() {
    return get();
  }
}
