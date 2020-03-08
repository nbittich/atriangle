package tech.artcoded.atriangle.rest.controller;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.IdGenerators;
import tech.artcoded.atriangle.api.ModelConverter;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.ShaclValidator;
import tech.artcoded.atriangle.api.kafka.EventType;
import tech.artcoded.atriangle.api.kafka.KafkaEvent;
import tech.artcoded.atriangle.api.kafka.RestEvent;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.feign.clients.FileRestFeignClient;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static tech.artcoded.atriangle.core.rest.util.RestUtil.FILE_TO_JSON;


@CrossOriginRestController
@RequestMapping("/api/rdf-ingest")
@ApiOperation("Rdf Ingestion")
@Slf4j
public class RdfIngestionController implements PingControllerTrait {
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapperWrapper objectMapperWrapper;
  private final FileRestFeignClient fileRestFeignClient;

  @Inject
  public RdfIngestionController(
    KafkaTemplate<String, String> kafkaTemplate, ObjectMapperWrapper objectMapperWrapper,
    FileRestFeignClient fileRestFeignClient) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapperWrapper = objectMapperWrapper;
    this.fileRestFeignClient = fileRestFeignClient;
  }

  @Value("${shacl.enabled:false}")
  private boolean shaclEnabled;

  @Value("${spring.kafka.template.default-topic}")
  private String topicProducer;

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> ingest(@RequestParam("graphUri") String graphUri,
                                       @RequestParam(value = "elasticIndex") String elasticIndex,
                                       @RequestParam(value = "createIndex", defaultValue = "false") boolean createIndex,
                                       @RequestParam("rdfFile") MultipartFile rdfFile,
                                       @RequestParam(value = "elasticSettings",
                                                     required = false) MultipartFile settingsFile,
                                       @RequestParam(value = "shaclModel",
                                                     required = false) MultipartFile shaclModel,
                                       @RequestParam(value = "elasticMappings",
                                                     required = false) MultipartFile mappingsFile
  ) {
    Model inputModel = ModelConverter.inputStreamToModel(requireNonNull(getExtension(rdfFile.getOriginalFilename())), rdfFile::getInputStream);
    Optional<Model> validationErrors = Optional.ofNullable(shaclModel)
                                               .flatMap(shaclFile -> ShaclValidator.validateModel(inputModel, shaclEnabled, getExtension(shaclFile.getOriginalFilename()), shaclFile::getInputStream));

    if (validationErrors.isPresent()) {
      return ResponseEntity.badRequest()
                           .body(ModelConverter.modelToLang(validationErrors.get(), Lang.JSONLD));
    }
    else {
      log.info("model valid or shacl disabled or shacl config file not provided");
    }

    String json = ModelConverter.modelToLang(inputModel, Lang.JSONLD);

    log.info("request payload in json '{}'", json);

    RestEvent restEvent = RestEvent.builder()
                                   .graphUri(graphUri)
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

    return ResponseEntity.accepted()
                         .body(json);
  }


  @GetMapping("/ping-file-endpoint")
  public ResponseEntity<Map<String, String>> pingFileEndpoint() {
    return this.fileRestFeignClient.ping();
  }
}
