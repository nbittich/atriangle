package tech.artcoded.atriangle.rest.project;

import feign.FeignException;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class FeignExceptionHandler {

  @SneakyThrows
  @ExceptionHandler({FeignException.class})
  public ResponseEntity<String> handleFeignStatusException(FeignException e, HttpServletResponse response) {
    response.setStatus(e.status());
    return ResponseEntity.status(e.status()).body(e.contentUTF8());
  }

}