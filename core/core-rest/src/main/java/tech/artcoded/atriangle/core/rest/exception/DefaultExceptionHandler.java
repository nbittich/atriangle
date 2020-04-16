package tech.artcoded.atriangle.core.rest.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class DefaultExceptionHandler {
  @ExceptionHandler({RuntimeException.class})
  public ResponseEntity<String> runtimeException(WebRequest webRequest, Exception exception) {
    return ResponseEntity.badRequest()
                         .body(exception.getMessage());
  }
}
