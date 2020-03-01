package tech.artcoded.atriangle.rest.controller;

import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.StmtIterator;
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
import org.topbraid.shacl.vocabulary.SH;
import tech.artcoded.atriangle.api.CheckedSupplier;
import tech.artcoded.atriangle.api.ModelConverter;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.ShaclValidator;
import tech.artcoded.atriangle.api.kafka.ElasticEvent;
import tech.artcoded.atriangle.api.kafka.EventType;
import tech.artcoded.atriangle.api.kafka.KafkaEvent;
import tech.artcoded.atriangle.api.kafka.RdfEvent;
import tech.artcoded.atriangle.rest.annotation.CrossOriginRestController;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
  @Value("${shacl.enabled:false}")
  private boolean shaclEnabled;

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
                                       @RequestParam(value = "shaclModel",
                                                     required = false) MultipartFile shaclModel,
                                       @RequestParam(value = "elasticMappings",
                                                     required = false) MultipartFile mappingsFile
  ) throws Exception {


    CheckedSupplier<InputStream> checkedSupplier = rdfFile::getInputStream;
    Model inputModel = ModelConverter.inputStreamToModel(FilenameUtils.getExtension(rdfFile.getOriginalFilename()), checkedSupplier.safeGet());

    Optional<Model> validationErrors = Optional.ofNullable(shaclModel)
                                               .flatMap(shaclFile -> validateModel(inputModel, shaclFile));

    if (validationErrors.isPresent()) {
      return ResponseEntity.badRequest()
                           .body(ModelConverter.modelToLang(validationErrors.get(), Lang.JSONLD));
    }
    else {
      log.info("model valid or shacl disabled or shacl config file not provided");
    }

    String json = ModelConverter.modelToLang(inputModel, Lang.JSONLD);

    log.info("request payload in json '{}'", json);
    produceKafkaEvent(graphUri,elasticIndex,createIndex,settingsFile,mappingsFile, json);

    return ResponseEntity.accepted().build();
  }

  private void produceKafkaEvent(String graphUri, String elasticIndex, boolean createIndex,
                                 MultipartFile settingsFile,  MultipartFile mappingsFile, String json) {
    CompletableFuture.runAsync(()->{
      String index = Optional.ofNullable(elasticIndex)
                             .filter(String::isEmpty)
                             .orElse(defaultElasticIndex);

      log.info("elastic search index to use '{}'", index);


      String rdfSinkEventId = ID_SUPPLIER.get();
      String elasticSinkEventId = ID_SUPPLIER.get();

      RdfEvent rdfSinkEvent = RdfEvent.builder()
                                      .graphUri(graphUri)
                                      .build();
      KafkaEvent kafkaEventForRdf = KafkaEvent.builder()
                                              .id(rdfSinkEventId)
                                              .json(json)
                                              .eventType(EventType.RDF_SINK)
                                              .event(objectMapperWrapper.serialize(rdfSinkEvent))
                                              .build();

      ElasticEvent elasticEvent = ElasticEvent.builder()
                                              .index(index)
                                              .createIndex(createIndex)
                                              .settings(FILE_TO_JSON.apply(settingsFile))
                                              .mappings(FILE_TO_JSON.apply(mappingsFile))
                                              .build();


      KafkaEvent kafkaEventForElastic = KafkaEvent.builder()
                                                  .id(elasticSinkEventId)
                                                  .json(json)
                                                  .eventType(EventType.ELASTIC_SINK)
                                                  .event(objectMapperWrapper.serialize(elasticEvent))
                                                  .build();

      log.info("sending to topic dispatcher");

      ProducerRecord<String, String> rdfRecord = new ProducerRecord<>(topicProducer, rdfSinkEventId, objectMapperWrapper.serialize(kafkaEventForRdf));
      ProducerRecord<String, String> elasticRecord = new ProducerRecord<>(topicProducer, elasticSinkEventId, objectMapperWrapper.serialize(kafkaEventForElastic));


      log.info("sending rdf record");
      kafkaTemplate.send(rdfRecord);
      log.info("sending elastic record");
      kafkaTemplate.send(elasticRecord);
    });
  }

  @SneakyThrows
  private Optional<Model> validateModel(Model inputModel, MultipartFile shaclFile) {

    if (!shaclEnabled) return Optional.empty();

    InputStream inputStream = shaclFile.getInputStream();
    ShaclValidator validator = () -> ModelConverter.inputStreamToModel(FilenameUtils.getExtension(shaclFile.getOriginalFilename()), inputStream);

    Model validationModel = validator.validate(inputModel);

    StmtIterator isConforms = validationModel.listStatements(null, SH.conforms, (RDFNode) null);
    boolean conform = isConforms.hasNext() && isConforms.nextStatement()
                                                        .getBoolean();
    if (!conform) {
      return Optional.of(validationModel);
    }
    return Optional.empty();
  }
}
