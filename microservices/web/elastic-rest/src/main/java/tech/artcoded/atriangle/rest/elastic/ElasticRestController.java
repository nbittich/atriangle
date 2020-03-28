package tech.artcoded.atriangle.rest.elastic;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.LogEvent;
import tech.artcoded.atriangle.core.elastic.ElasticSearchRdfService;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.controller.BuildInfoControllerTrait;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.feign.clients.elastic.ElasticRestFeignClient;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CrossOriginRestController
@Slf4j
public class ElasticRestController implements PingControllerTrait, BuildInfoControllerTrait, ElasticRestFeignClient {
  @Getter
  private final BuildProperties buildProperties;
  private final ElasticSearchRdfService elasticSearchRdfService;
  private final ObjectMapperWrapper mapperWrapper;

  @Value("${elasticsearch.shared-indexes.logsink}")
  private String logSinkIndex;

  @Inject
  public ElasticRestController(BuildProperties buildProperties,
                               ElasticSearchRdfService elasticSearchRdfService,
                               ObjectMapperWrapper mapperWrapper) {
    this.buildProperties = buildProperties;
    this.elasticSearchRdfService = elasticSearchRdfService;
    this.mapperWrapper = mapperWrapper;
  }

  @Override
  public List<LogEvent> getLogsByCorrelationId(String correlationId) {
    SearchResponse searchResponse = elasticSearchRdfService.matchQuery("correlationId", correlationId, logSinkIndex);
    return Stream.of(searchResponse.getHits()
                                   .getHits())
                 .map(hit -> mapperWrapper.deserialize(hit.getSourceAsString(), LogEvent.class))
                 .flatMap(Optional::stream)
                 .sorted(Comparator.comparing(LogEvent::getCreationDate, Date::compareTo))
                 .collect(Collectors.toUnmodifiableList());
  }

}
