package tech.artcoded.atriangle.rest.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Function;

public interface RestUtil {
  Logger LOGGER = LoggerFactory.getLogger(RestUtil.class);
  Function<MultipartFile, String> FILE_TO_JSON = file -> Optional.ofNullable(file)
                                                                 .map(f -> {
                                                                   try (var is = f.getInputStream()) {
                                                                     return IOUtils.toString(is, StandardCharsets.UTF_8);
                                                                   }
                                                                   catch (Exception e) {
                                                                     LOGGER.info("error transforming file", e);
                                                                     return null;
                                                                   }
                                                                 })
                                                                 .orElse("{}");


}
