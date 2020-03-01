package tech.artcoded.atriangle.rest.controller;

import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.apache.jena.riot.Lang;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.ModelConverter;
import tech.artcoded.atriangle.api.kafka.KafkaEvent;
import tech.artcoded.atriangle.api.kafka.RdfEvent;

import javax.inject.Inject;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/rdf-ingest")
public class RdfIngestionController {
  private final KafkaTemplate<String, String> kafkaTemplate;

  @Inject
  public RdfIngestionController(
    KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @SneakyThrows
  @PostMapping
  public ResponseEntity<Void> ingest(@RequestParam("graphUri") String graphUri,
                                     @RequestParam("file") MultipartFile file
  ) {

    String json = ModelConverter.inputStreamToLang(FilenameUtils.getExtension(file.getOriginalFilename()), file.getInputStream(), Lang.JSONLD);

    RdfEvent rdfSinkEvent = RdfEvent.builder()
                                    .graphUri(graphUri)
                                    .id(UUID.randomUUID()
                                            .toString())
                                    .eventType(KafkaEvent.EventType.RDF_SINK)
                                    .json(json)
                                    .build();

    return ResponseEntity.accepted()
                         .build();
  }
}
