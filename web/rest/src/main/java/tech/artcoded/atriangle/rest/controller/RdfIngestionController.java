package tech.artcoded.atriangle.rest.controller;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.openrdf.model.Model;
import org.openrdf.rio.RDFFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.IdGenerators;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.kafka.EventType;
import tech.artcoded.atriangle.api.kafka.KafkaEvent;
import tech.artcoded.atriangle.api.kafka.RestEvent;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.core.sparql.ModelConverter;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;
import static tech.artcoded.atriangle.core.rest.util.RestUtil.FILE_TO_JSON;


@CrossOriginRestController
@RequestMapping("/api/rdf-ingest")
@ApiOperation("Rdf Ingestion")
@Slf4j
public class RdfIngestionController implements PingControllerTrait {
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapperWrapper objectMapperWrapper;

  @Inject
  public RdfIngestionController(
    KafkaTemplate<String, String> kafkaTemplate, ObjectMapperWrapper objectMapperWrapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapperWrapper = objectMapperWrapper;
  }

  @Value("${shacl.enabled:false}")
  private boolean shaclEnabled;

  @Value("${spring.kafka.template.default-topic}")
  private String topicProducer;

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, String>> ingest(@RequestParam("namespace") String namespace,
                                                    @RequestParam(value = "elasticIndex") String elasticIndex,
                                                    @RequestParam(value = "createIndex",
                                                                  defaultValue = "false") boolean createIndex,
                                                    @RequestParam("rdfFile") MultipartFile rdfFile,
                                                    @RequestParam(value = "elasticSettings",
                                                                  required = false) MultipartFile settingsFile,
                                                    @RequestParam(value = "elasticMappings",
                                                                  required = false) MultipartFile mappingsFile
  ) {
    asyncSendEvent(namespace, elasticIndex, createIndex, rdfFile, settingsFile, mappingsFile);
    return ResponseEntity.accepted().body(Map.of("message", "RDF will be processed"));
  }

  private void asyncSendEvent(String namespace,
                              String elasticIndex,
                              boolean createIndex,
                              MultipartFile rdfFile,
                              MultipartFile settingsFile,
                              MultipartFile mappingsFile) {
    CompletableFuture.runAsync(() -> {
      Model inputModel = ModelConverter.inputStreamToModel(requireNonNull(rdfFile.getOriginalFilename()), rdfFile::getInputStream);

      String json = ModelConverter.modelToLang(inputModel, RDFFormat.JSONLD);

      log.debug("request payload in json '{}'", json);

      RestEvent restEvent = RestEvent.builder()
                                     .namespace(namespace)
                                     .elasticIndex(elasticIndex)
                                     .createIndex(createIndex)
                                     .elasticSettingsJson(FILE_TO_JSON.apply(settingsFile))
                                     .elasticMappingsJson(FILE_TO_JSON.apply(mappingsFile))
                                     .build();

      KafkaEvent kafkaEvent = KafkaEvent.builder()
                                        .eventType(EventType.REST_SINK)
                                        .id(IdGenerators.UUID_SUPPLIER.get())
                                        .json(json)
                                        .event(objectMapperWrapper.serialize(restEvent))
                                        .build();

      ProducerRecord<String, String> restRecord = new ProducerRecord<>(topicProducer, kafkaEvent.getId(), objectMapperWrapper.serialize(kafkaEvent));

      kafkaTemplate.send(restRecord);
    });
  }

}
