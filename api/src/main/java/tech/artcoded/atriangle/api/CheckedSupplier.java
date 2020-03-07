package tech.artcoded.atriangle.api;

import lombok.SneakyThrows;

@FunctionalInterface
public interface CheckedSupplier<T> {
  T get() throws Exception;

  @SneakyThrows
  default T safeGet() {
    return get();
  }
}
