package tech.artcoded.atriangle.core.rest.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.dto.FileEvent;

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

  static ResponseEntity<ByteArrayResource> transformToByteArrayResource(FileEvent event, byte[] file) {
    return Optional.ofNullable(event)
                   .map(u -> ResponseEntity.ok()
                                           .contentType(MediaType.parseMediaType(u.getContentType()))
                                           .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + u.getOriginalFilename() + "\"")
                                           .body(new ByteArrayResource(file)))
                   .orElseGet(ResponseEntity.notFound()::build);
  }



}
