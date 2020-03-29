package tech.artcoded.atriangle.rest.elastic;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import tech.artcoded.atriangle.api.IdGenerators;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.LogEvent;
import tech.artcoded.atriangle.core.elastic.ElasticSearchRdfService;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.controller.BuildInfoControllerTrait;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.feign.clients.elastic.ElasticRestFeignClient;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.*;
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

  @Override
  public ResponseEntity<String> createIndex(String indexName,
                                            boolean deleteIndexIfExist,
                                            String elasticConfiguration) {

    boolean indexExist = elasticSearchRdfService.indexExist(indexName);

    if (indexExist) {
      if(!deleteIndexIfExist){
        return ResponseEntity.badRequest()
                             .body("index already exists");
      }
      log.info("deleting index {}", indexName);
      AcknowledgedResponse acknowledgedResponse = elasticSearchRdfService.deleteIndex(indexName);
      log.info("delete index response: {}", acknowledgedResponse.isAcknowledged());
    }

    CreateIndexResponse response = elasticSearchRdfService.createIndex(indexName, IOUtils.toInputStream(Optional.ofNullable(elasticConfiguration)
                                                                                                                .orElse("{}"), StandardCharsets.UTF_8));
    log.info("index creation response: {}", response.isAcknowledged());
    return ResponseEntity.ok("elastic index created");

  }

  @Override
  public ResponseEntity<String> deleteIndex(String indexName) {
    AcknowledgedResponse acknowledgedResponse = elasticSearchRdfService.deleteIndex(indexName);
    log.info("delete index ack {}", acknowledgedResponse.isAcknowledged());
    return ResponseEntity.ok("index deleted");
  }

  @Override
  public ResponseEntity<String> deleteDocument(String indexName, String uuid) {
    DeleteResponse deleteResponse = elasticSearchRdfService.deleteDocument(indexName, uuid);
    log.info("delete document result {}", deleteResponse.getResult()
                                                        .getLowercase());
    return ResponseEntity.ok("document deleted");
  }

  @Override
  public ResponseEntity<String> index(String indexName, String document) {
    elasticSearchRdfService.indexAsync(indexName, IdGenerators.get(), document);
    return ResponseEntity.ok("resource indexed on elastic");
  }

  @Override
  public ResponseEntity<Set<String>> indices() {
    return ResponseEntity.ok(elasticSearchRdfService.indices());
  }

  @Override
  public ResponseEntity<String> search(String indexName, String request) {
    SearchResponse searchResponse = elasticSearchRdfService.rawSearch(indexName, request);
    String jsonResponse = searchResponse.toString();
    return ResponseEntity.ok(jsonResponse);
  }

  @Override
  public ResponseEntity<String> findAll(String indexName) {
    SearchResponse searchResponse = elasticSearchRdfService.searchAll(indexName);
    String jsonResponse = searchResponse.toString();
    return ResponseEntity.ok(jsonResponse);
  }

}
