package tech.artcoded.atriangle.eventdispatcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.kafka.KafkaEvent;

import javax.inject.Inject;
import java.util.Optional;

@Component
@Slf4j
public class EventDispatcherSink {
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapperWrapper mapperWrapper;

  @Value("${event.dispatcher.elastic-sink-topic}")
  private String elsticSinkTopic;
  @Value("${event.dispatcher.rdf-sink-topic}")
  private String rdfSinkTopic;


  @Inject
  public EventDispatcherSink(KafkaTemplate<String, String> kafkaTemplate,
                             ObjectMapperWrapper mapperWrapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = mapperWrapper;
  }

  @KafkaListener(topics = {"${event.dispatcher.elastic-sink-topic-out}", "${event.dispatcher.rdf-sink-topic-out}"})
  public void logOutput(ConsumerRecord<String, String> event) throws Exception {
    log.info("receiving output event with id {} and value {}", event.key(), event.value());
  }

  @KafkaListener(topics = "${spring.kafka.template.default-topic}")
  public void dispatch(ConsumerRecord<String, String> event) throws Exception {
    String value = event.value();
    log.info("receiving key {} value {}", event.key(), event.value());
    Optional<KafkaEvent> optionalKafkaEvent = mapperWrapper.deserialize(value, KafkaEvent.class);
    optionalKafkaEvent.ifPresent(kafkaEvent -> {
      switch (kafkaEvent.getEventType()) {
        case RDF_SINK:
          kafkaTemplate.send(new ProducerRecord<>(rdfSinkTopic, event.key(), event.value()));
          break;
        case FILE_SINK:
          log.info("file sink is disabled, event filtered ");
        case ELASTIC_SINK:
          kafkaTemplate.send(new ProducerRecord<>(elsticSinkTopic, event.key(), event.value()));
          break;
      }
    });
  }

}
