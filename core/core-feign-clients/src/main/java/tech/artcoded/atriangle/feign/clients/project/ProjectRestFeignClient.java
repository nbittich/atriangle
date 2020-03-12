package tech.artcoded.atriangle.feign.clients.project;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tech.artcoded.atriangle.api.kafka.ProjectEvent;

import java.util.List;

public interface ProjectRestFeignClient {
  @PostMapping
  ResponseEntity<ProjectEvent> createProject(@RequestParam("name") String name);

  @GetMapping("/list")
  List<ProjectEvent> findAll();
}
