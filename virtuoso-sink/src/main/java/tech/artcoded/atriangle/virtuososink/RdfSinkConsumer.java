package tech.artcoded.atriangle.virtuososink;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.ModelConverter;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.SparqlService;
import tech.artcoded.atriangle.api.kafka.KafkaEvent;
import tech.artcoded.atriangle.api.kafka.RdfEvent;
import tech.artcoded.atriangle.core.kafka.ATriangleConsumer;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class RdfSinkConsumer implements ATriangleConsumer<String, String> {
  private final SparqlService sparqlService;

  private final KafkaTemplate<String,String> kafkaTemplate;
  private final ObjectMapperWrapper mapperWrapper;

  @Value("${out.topic}")
  private String outTopic;

  @Inject
  public RdfSinkConsumer(SparqlService sparqlService,
                         KafkaTemplate<String, String> kafkaTemplate,
                         ObjectMapperWrapper mapperWrapper) {
    this.sparqlService = sparqlService;
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = mapperWrapper;
  }

  @Override
  public Map.Entry<String, String> consume(ConsumerRecord<String, String> record) {
    String rdfEvent = record.value();

    Optional<KafkaEvent> optionalKafkaEvent = mapperWrapper.deserialize(rdfEvent, KafkaEvent.class);
    KafkaEvent kafkaEvent = optionalKafkaEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

    Optional<RdfEvent> optionalRdfEvent = mapperWrapper.deserialize(kafkaEvent.getEvent(), RdfEvent.class);
    RdfEvent event = optionalRdfEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

    log.info("converting to model");

    Model model = ModelConverter.toModel(kafkaEvent.getJson(), Lang.JSONLD);
    model.write(System.out, Lang.TURTLE.getLabel());

    log.info("saving to triplestore");
    sparqlService.upload(event.getGraphUri(), model);
    log.info("saved to triplestore");
    return Map.entry(UUID.randomUUID()
                         .toString(), mapperWrapper.serialize(Map.of("ack", "true",
                                                                     "id", kafkaEvent.getId())));
  }

  @Override
  @KafkaListener(topics = "${spring.kafka.template.default-topic}")
  public void sink(ConsumerRecord<String, String> record) throws Exception {
    log.info("receiving key {} value {}", record.key(), record.value());
    Map.Entry<String, String> response = consume(record);
    kafkaTemplate.send(new ProducerRecord<>(outTopic, response.getKey(), response.getValue()));
  }

}
