package tech.artcoded.atriangle.core.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import tech.artcoded.atriangle.core.rest.annotation.SwaggerHeaderAuthentication;

import java.util.Map;

public interface PingControllerTrait {
  @GetMapping("/ping")
  @SwaggerHeaderAuthentication
  default ResponseEntity<Map<String, String>> ping() {
    return ResponseEntity.ok(Map.of("message", "pong"));
  }
}
