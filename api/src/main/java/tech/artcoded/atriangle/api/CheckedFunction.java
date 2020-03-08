package tech.artcoded.atriangle.api;

import lombok.SneakyThrows;

@FunctionalInterface
public interface CheckedFunction<I, O> {
  O get(I input) throws Exception;

  @SneakyThrows
  default O safeExecute(I input) {
    return get(input);
  }
}
