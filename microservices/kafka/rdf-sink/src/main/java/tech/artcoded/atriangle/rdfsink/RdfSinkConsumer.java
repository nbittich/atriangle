package tech.artcoded.atriangle.rdfsink;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.openrdf.rio.RDFFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.DateHelper;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.FileEvent;
import tech.artcoded.atriangle.api.dto.FileEventType;
import tech.artcoded.atriangle.api.dto.KafkaEvent;
import tech.artcoded.atriangle.api.dto.RestEvent;
import tech.artcoded.atriangle.core.kafka.ATriangleConsumer;
import tech.artcoded.atriangle.core.kafka.LoggerAction;
import tech.artcoded.atriangle.core.sparql.ModelConverter;
import tech.artcoded.atriangle.core.sparql.SimpleSparqlService;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;
import tech.artcoded.atriangle.feign.clients.shacl.ShaclRestFeignClient;
import tech.artcoded.atriangle.feign.clients.util.FeignMultipartFile;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class RdfSinkConsumer implements ATriangleConsumer<String, String> {

  private static final String DERIVED_FILE_REGEX = "-derived-output-";
  private static final String DERIVED_FILE_JSON_LD_REGEX = DERIVED_FILE_REGEX + "jsonld-";

  private final SimpleSparqlService sparqlService;
  private final FileRestFeignClient fileRestFeignClient;
  private final ShaclRestFeignClient shaclRestFeignClient;

  @Getter
  @Value("${out.topic}")
  private String outTopic;

  @Getter
  private final KafkaTemplate<String, String> kafkaTemplate;

  private final ObjectMapperWrapper mapperWrapper;
  private final RdfSinkOutputProducer rdfSinkOutputProducer;
  private final LoggerAction loggerAction;


  @Inject
  public RdfSinkConsumer(SimpleSparqlService sparqlService,
                         FileRestFeignClient fileRestFeignClient,
                         ShaclRestFeignClient shaclRestFeignClient,
                         KafkaTemplate<String, String> kafkaTemplate,
                         ObjectMapperWrapper mapperWrapper,
                         RdfSinkOutputProducer rdfSinkOutputProducer,
                         LoggerAction loggerAction) {
    this.sparqlService = sparqlService;
    this.fileRestFeignClient = fileRestFeignClient;
    this.shaclRestFeignClient = shaclRestFeignClient;
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = mapperWrapper;
    this.rdfSinkOutputProducer = rdfSinkOutputProducer;
    this.loggerAction = loggerAction;
  }

  @Override
  public Map<String, String> consume(ConsumerRecord<String, String> record) throws Exception {
    String restEvent = record.value();

    Optional<KafkaEvent> optionalKafkaEvent = mapperWrapper.deserialize(restEvent, KafkaEvent.class);
    KafkaEvent kafkaEvent = optionalKafkaEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

    Optional<RestEvent> optionalRestEvent = mapperWrapper.deserialize(kafkaEvent.getEvent(), RestEvent.class);
    RestEvent event = optionalRestEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

    FileEvent inputToSinkFileEvent = kafkaEvent.getInputToSink();
    ResponseEntity<ByteArrayResource> inputToSink = fileRestFeignClient.download(inputToSinkFileEvent.getId());

    if (kafkaEvent.getShaclModel() != null) {
      log.info("shacl validation");
      ResponseEntity<String> validate = shaclRestFeignClient.validate(inputToSinkFileEvent.getId(), kafkaEvent.getShaclModel()
                                                                                                              .getId());
      if (validate.getStatusCodeValue() != HttpStatus.OK.value() || StringUtils.isNotEmpty(validate.getBody())) {
        log.error("validation failed {}", validate.getBody());
        loggerAction.error(kafkaEvent::getCorrelationId, String.format("validation shacl failed for event %s, result %s", kafkaEvent
          .getId(), validate
                                                                         .getBody()));
        throw new RuntimeException();
      }
    }

    loggerAction.info(kafkaEvent::getCorrelationId, "shacl validation ok");

    log.info("saving to triplestore");

    sparqlService.load(event.getNamespace(), inputToSink.getBody()
                                                        .getInputStream(), RDFFormat.forFileName(inputToSinkFileEvent.getName()));

    loggerAction.info(kafkaEvent::getCorrelationId, "saved to triplestore");

    log.info("create derivate json-ld rdf file");

    String baseFileName = FilenameUtils.removeExtension(inputToSinkFileEvent.getOriginalFilename()) + "-" + RandomStringUtils.randomAlphanumeric(3);
    String outputFilename = baseFileName + DERIVED_FILE_JSON_LD_REGEX + DateHelper.formatCurrentDateForFilename() + ".json";
    String jsonld = ModelConverter.inputStreamToLang(inputToSinkFileEvent.getName(), inputToSink.getBody()::getInputStream, RDFFormat.JSONLD);
    MultipartFile rdfOutput = FeignMultipartFile.builder().contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .name(outputFilename)
                                                .originalFilename(outputFilename)
                                                .bytes(jsonld.getBytes())
                                                .build();

    ResponseEntity<FileEvent> derivatedFile = fileRestFeignClient.upload(rdfOutput, FileEventType.RDF_TO_JSON_LD_OUTPUT);

    log.info("sending other events");


    FileEvent jsonLdFile = derivatedFile.getBody();

    return rdfSinkOutputProducer.produce(kafkaEvent, event, jsonLdFile);

  }


}