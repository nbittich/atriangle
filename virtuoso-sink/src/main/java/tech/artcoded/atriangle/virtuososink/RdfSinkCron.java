package tech.artcoded.atriangle.virtuososink;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.ModelConverter;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.SparqlService;
import tech.artcoded.atriangle.api.kafka.RdfEvent;
import tech.artcoded.atriangle.api.kafka.SimpleKafkaTemplate;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class RdfSinkCron {
  private final SparqlService sparqlService;
  private final ObjectMapperWrapper mapperWrapper;
  private final SimpleKafkaTemplate kafkaTemplate;


  @Inject
  public RdfSinkCron(SparqlService sparqlService,
                     ObjectMapperWrapper mapperWrapper,
                     SimpleKafkaTemplate kafkaTemplate) {
    this.sparqlService = sparqlService;
    this.mapperWrapper = mapperWrapper;
    this.kafkaTemplate = kafkaTemplate;
  }

  @Scheduled(cron = "${virtuososink.cron}")
  public void sink() {
    log.info("sink started...");
    kafkaTemplate.consume(300L, ChronoUnit.MILLIS, consumerRecords -> {
      consumerRecords.forEach(record -> {
        String rdfEvent = record.value();
        Optional<RdfEvent> optionalRdfEvent = mapperWrapper.deserialize(rdfEvent, RdfEvent.class);
        RdfEvent event = optionalRdfEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

        Model model = ModelConverter.toModel(event.getJson(), Lang.JSONLD);

        sparqlService.insertOrUpdateToGraph(event.getGraphUri(), model);

        kafkaTemplate.produce(UUID.randomUUID()
                                  .toString(), mapperWrapper.serialize(Map.of("ack", "true",
                                                                              "id", event.getId())));

      });
    });
    log.info("sink ended...");

  }
}
