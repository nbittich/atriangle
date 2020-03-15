package tech.artcoded.atriangle.api;

import lombok.SneakyThrows;

import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction<I, O> {
  static <I, O> Function<I, O> toFunction(CheckedFunction<I, O> hack) {
    return hack::safeApply;
  }

  O apply(I input) throws Exception;

  @SneakyThrows
  default O safeApply(I input) {
    return apply(input);
  }
}
