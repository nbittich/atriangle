package tech.artcoded.atriangle.api;

import java.io.IOException;
import java.util.function.Supplier;

@FunctionalInterface
public interface CheckedSupplier<T> {
  T get() throws IOException;
}
