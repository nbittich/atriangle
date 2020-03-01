package tech.artcoded.atriangle.rest.util;

import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public interface RestUtil {
  Function<MultipartFile, String> FILE_TO_JSON = file -> Optional.ofNullable(file)
                                                                 .map(f -> {
                                                                   try (var is = f.getInputStream()) {
                                                                     return IOUtils.toString(is, StandardCharsets.UTF_8);
                                                                   }
                                                                   catch (Exception e) {
                                                                     return null;
                                                                   }
                                                                 })
                                                                 .orElse("{}");

  Supplier<String> ID_SUPPLIER = () -> UUID.randomUUID()
                                           .toString();
}
