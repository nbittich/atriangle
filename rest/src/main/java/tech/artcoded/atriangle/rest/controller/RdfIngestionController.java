package tech.artcoded.atriangle.rest.controller;

import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.jena.riot.Lang;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.ModelConverter;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.kafka.ElasticEvent;
import tech.artcoded.atriangle.api.kafka.RdfEvent;
import tech.artcoded.atriangle.rest.annotation.CrossOriginRestController;

import javax.inject.Inject;
import java.util.Optional;

import static tech.artcoded.atriangle.rest.util.RestUtil.FILE_TO_JSON;
import static tech.artcoded.atriangle.rest.util.RestUtil.ID_SUPPLIER;

@CrossOriginRestController
@RequestMapping("/api/rdf-ingest")
@ApiOperation("Rdf Ingestion")
@Slf4j
public class RdfIngestionController {
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapperWrapper objectMapperWrapper;

  @Inject
  public RdfIngestionController(
    KafkaTemplate<String, String> kafkaTemplate, ObjectMapperWrapper objectMapperWrapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapperWrapper = objectMapperWrapper;
  }

  @Value("${elasticsearch.index}")
  private String defaultElasticIndex;

  @Value("${spring.kafka.template.default-topic}")
  private String topicProducer;

  @SneakyThrows
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> ingest(@RequestParam("graphUri") String graphUri,
                                       @RequestParam(value = "elasticIndex", required = false) String elasticIndex,
                                       @RequestParam(value = "createIndex", defaultValue = "false") boolean createIndex,
                                       @RequestParam("rdfFile") MultipartFile rdfFile,
                                       @RequestParam(value = "elasticSettings",
                                                     required = false) MultipartFile settingsFile,
                                       @RequestParam(value = "elasticMappings",
                                                     required = false) MultipartFile mappingsFile
  ) throws Exception {

    String index = Optional.ofNullable(elasticIndex)
                           .filter(String::isEmpty)
                           .orElse(defaultElasticIndex);

    log.info("elastic search index to use '{}'", index);

    String json = ModelConverter.inputStreamToLang(FilenameUtils.getExtension(rdfFile.getOriginalFilename()), rdfFile.getInputStream(), Lang.JSONLD);

    log.info("request payload in json '{}'", json);

    String rdfSinkEventId = ID_SUPPLIER.get();
    String elasticSinkEventId = ID_SUPPLIER.get();

    RdfEvent rdfSinkEvent = RdfEvent.builder()
                                    .graphUri(graphUri)
                                    .id(rdfSinkEventId)
                                    .json(json)
                                    .build();


    ElasticEvent elasticEvent = ElasticEvent.builder()
                                            .index(index)
                                            .id(elasticSinkEventId)
                                            .createIndex(createIndex)
                                            .settings(FILE_TO_JSON.apply(settingsFile))
                                            .mappings(FILE_TO_JSON.apply(mappingsFile))
                                            .build();

    log.info("sending to topic dispatcher");

    ProducerRecord<String, String> rdfRecord = new ProducerRecord<>(topicProducer, rdfSinkEventId, objectMapperWrapper.serialize(rdfSinkEvent));
    ProducerRecord<String, String> elasticRecord = new ProducerRecord<>(topicProducer, elasticSinkEventId, objectMapperWrapper.serialize(elasticEvent));

    Boolean result = kafkaTemplate.executeInTransaction(kafkaOperations -> {
      log.info("sending rdf record");
      kafkaOperations.send(rdfRecord);
      log.info("sending elastic record");
      kafkaOperations.send(elasticRecord);
      return true; // todo
    });


    return ResponseEntity.accepted()
                         .body(json);
  }
}
