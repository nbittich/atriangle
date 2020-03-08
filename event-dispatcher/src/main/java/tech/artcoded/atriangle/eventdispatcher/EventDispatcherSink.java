package tech.artcoded.atriangle.eventdispatcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.CheckedFunction;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.kafka.KafkaEvent;
import tech.artcoded.atriangle.core.kafka.LoggerAction;

import javax.inject.Inject;
import java.util.Optional;

@Component
@Slf4j
public class EventDispatcherSink {
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final LoggerAction loggerAction;
  private final ObjectMapperWrapper mapperWrapper;

  @Value("${event.dispatcher.elastic-sink-topic}")
  private String elsticSinkTopic;
  @Value("${event.dispatcher.rdf-sink-topic}")
  private String rdfSinkTopic;


  @Inject
  public EventDispatcherSink(KafkaTemplate<String, String> kafkaTemplate,
                             LoggerAction loggerAction,
                             ObjectMapperWrapper mapperWrapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.loggerAction = loggerAction;
    this.mapperWrapper = mapperWrapper;
  }

  @KafkaListener(topics = {"${event.dispatcher.elastic-sink-topic-out}", "${event.dispatcher.rdf-sink-topic-out}"})
  public void logOutput(ConsumerRecord<String, String> event) throws Exception {
    loggerAction.info(event::key, "receiving key %s, value %s", event.key(), event.value());
  }

  @KafkaListener(topics = "${spring.kafka.template.default-topic}")
  public void dispatch(ConsumerRecord<String, String> event) throws Exception {
    String value = event.value();
    Optional<KafkaEvent> optionalKafkaEvent = mapperWrapper.deserialize(value, KafkaEvent.class);
    CheckedFunction<String, SendResult<String, String>> sendEvent = (topic) ->
      kafkaTemplate.send(new ProducerRecord<>(topic, event.key(), event.value()))
                   .get();
    optionalKafkaEvent.ifPresent(kafkaEvent -> {
      switch (kafkaEvent.getEventType()) {
        case RDF_SINK:
          log.info("result of send event {}", sendEvent.safeGet(rdfSinkTopic));
          break;
        case ELASTIC_SINK:
          log.info("result of send event {}", sendEvent.safeGet(elsticSinkTopic));
          break;
        default:
          throw new RuntimeException("not supported yet");
      }
    });
  }

}
