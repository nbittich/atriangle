package tech.artcoded.atriangle.feign.clients.elastic;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tech.artcoded.atriangle.api.dto.LogEvent;

import java.util.List;

public interface ElasticRestFeignClient {
  @GetMapping
  List<LogEvent> getLogsByCorrelationId(@RequestParam("correlationId") String correlationId);
}
