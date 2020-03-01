package tech.artcoded.atriangle.virtuososink;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.ModelConverter;
import tech.artcoded.atriangle.api.SparqlService;
import tech.artcoded.atriangle.api.kafka.RdfEvent;
import tech.artcoded.atriangle.core.kafka.ATriangleConsumer;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class RdfSinkConsumer extends ATriangleConsumer<String, String> {
  private final SparqlService sparqlService;


  @Inject
  public RdfSinkConsumer(SparqlService sparqlService) {
    this.sparqlService = sparqlService;
  }

  @Override
  public Map.Entry<String, String> consume(ConsumerRecord<String, String> record) {
    String rdfEvent = record.value();
    Optional<RdfEvent> optionalRdfEvent = mapperWrapper.deserialize(rdfEvent, RdfEvent.class);
    RdfEvent event = optionalRdfEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

    log.info("converting to model");

    Model model = ModelConverter.toModel(event.getJson(), Lang.JSONLD);

    log.info("saving to triplestore");
    sparqlService.insertOrUpdateToGraph(event.getGraphUri(), model);

    return Map.entry(UUID.randomUUID()
                         .toString(), mapperWrapper.serialize(Map.of("ack", "true",
                                                                     "id", event.getId())));
  }
}
