package tech.artcoded.atriangle.rdfsink;

import feign.FeignException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.DateHelper;
import tech.artcoded.atriangle.api.dto.*;
import tech.artcoded.atriangle.core.kafka.KafkaEventHelper;
import tech.artcoded.atriangle.core.kafka.KafkaSink;
import tech.artcoded.atriangle.core.kafka.LoggerAction;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;
import tech.artcoded.atriangle.feign.clients.shacl.ShaclRestFeignClient;
import tech.artcoded.atriangle.feign.clients.sparql.SparqlRestFeignClient;
import tech.artcoded.atriangle.feign.clients.util.FeignMultipartFile;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Component
@Slf4j
public class RdfSinkConsumer implements KafkaSink<String, String> {

  private static final String DERIVED_FILE_REGEX = "-derived-output-";
  private static final String DERIVED_FILE_JSON_LD_REGEX = DERIVED_FILE_REGEX + "jsonld-";

  private final SparqlRestFeignClient sparqlRestFeignClient;
  private final FileRestFeignClient fileRestFeignClient;
  private final ShaclRestFeignClient shaclRestFeignClient;

  @Getter private final KafkaTemplate<String, String> kafkaTemplate;

  private final KafkaEventHelper kafkaEventHelper;
  private final RdfSinkOutputProducer rdfSinkOutputProducer;
  private final LoggerAction loggerAction;

  @Inject
  public RdfSinkConsumer(
      SparqlRestFeignClient sparqlRestFeignClient,
      FileRestFeignClient fileRestFeignClient,
      ShaclRestFeignClient shaclRestFeignClient,
      KafkaTemplate<String, String> kafkaTemplate,
      KafkaEventHelper kafkaEventHelper,
      RdfSinkOutputProducer rdfSinkOutputProducer,
      LoggerAction loggerAction) {
    this.sparqlRestFeignClient = sparqlRestFeignClient;
    this.fileRestFeignClient = fileRestFeignClient;
    this.shaclRestFeignClient = shaclRestFeignClient;
    this.kafkaTemplate = kafkaTemplate;
    this.kafkaEventHelper = kafkaEventHelper;
    this.rdfSinkOutputProducer = rdfSinkOutputProducer;
    this.loggerAction = loggerAction;
  }

  private void checkFile(KafkaEvent kafkaEvent, FileEvent fileEvent) {
    FileEventType eventType =
        Optional.ofNullable(fileEvent).map(FileEvent::getEventType).orElse(FileEventType.RAW_FILE);
    String originalFilename = requireNonNull(fileEvent).getOriginalFilename();
    if (!FileEventType.SHACL_FILE.equals(eventType)
        && !FileEventType.RDF_FILE.equals(eventType)
        && !FileEventType.SKOS_PLAY_CONVERTER_OUTPUT.equals(eventType)
        && !Optional.ofNullable(sparqlRestFeignClient.checkFileFormat(originalFilename))
            .map(ResponseEntity::getBody)
            .orElse(false)) {
      loggerAction.error(
          kafkaEvent::getCorrelationId,
          "not an rdf/shacl file, event type %s, file name %s",
          eventType.name(),
          originalFilename);
      throw new RuntimeException("not an rdf file " + originalFilename);
    }
  }

  @Override
  public List<KafkaMessage<String, String>> consume(ConsumerRecord<String, String> record)
      throws Exception {
    String restEvent = record.value();

    KafkaEvent kafkaEvent = kafkaEventHelper.parseKafkaEvent(restEvent);
    RestEvent event = kafkaEventHelper.parseEvent(kafkaEvent, RestEvent.class);
    FileEvent inputToSinkFileEvent = event.getInputToSink();
    checkFile(kafkaEvent, inputToSinkFileEvent);
    boolean shaclValidationResult = shaclValidation(kafkaEvent, event, inputToSinkFileEvent);

    if (!shaclValidationResult) {
      return List.of();
    }

    log.info("saving to triplestore");

    ResponseEntity<String> jsonLdResponse =
        sparqlRestFeignClient.loadRdfFile(inputToSinkFileEvent.getId(), event.getNamespace());

    loggerAction.info(kafkaEvent::getCorrelationId, "saved to triplestore");

    log.info("create derivate json-ld rdf file");

    String baseFileName = FilenameUtils.removeExtension(inputToSinkFileEvent.getOriginalFilename());
    String outputFilename =
        baseFileName.split(DERIVED_FILE_REGEX)[0]
            + DERIVED_FILE_JSON_LD_REGEX
            + DateHelper.formatCurrentDateForFilename()
            + ".json";

    MultipartFile rdfOutput =
        FeignMultipartFile.builder()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .name(outputFilename)
            .originalFilename(outputFilename)
            .bytes(jsonLdResponse.getBody().getBytes())
            .build();

    ResponseEntity<FileEvent> derivatedFile =
        fileRestFeignClient.upload(
            rdfOutput, FileEventType.RDF_TO_JSON_LD_OUTPUT, kafkaEvent.getCorrelationId());

    log.info("sending other events");

    FileEvent jsonLdFile = derivatedFile.getBody();

    return rdfSinkOutputProducer.produce(
        kafkaEvent, event, jsonLdFile, record.partition(), record.offset(), record.headers());
  }

  private boolean shaclValidation(
      KafkaEvent kafkaEvent, RestEvent event, FileEvent inputToSinkFileEvent) {
    if (event.getShaclModel() != null) {
      log.info("shacl validation");
      try {
        ResponseEntity<String> validate =
            shaclRestFeignClient.validate(
                kafkaEvent.getCorrelationId(),
                event.getShaclModel().getId(),
                inputToSinkFileEvent.getId());

        if (validate.getStatusCodeValue() != HttpStatus.OK.value()
            || StringUtils.isNotEmpty(validate.getBody())) {
          loggerAction.error(kafkaEvent::getCorrelationId, validate.getBody());
          return false;
        }

        loggerAction.info(kafkaEvent::getCorrelationId, "shacl validation ok");
      } catch (FeignException exception) {
        exception
            .responseBody()
            .ifPresent(
                body -> {
                  String responseBodyAsString = new String(body.array());
                  loggerAction.error(kafkaEvent::getCorrelationId, responseBodyAsString);
                });
        return false;
      }
    }
    return true;
  }
}
