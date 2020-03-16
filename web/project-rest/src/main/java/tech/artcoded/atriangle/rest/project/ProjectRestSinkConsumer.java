package tech.artcoded.atriangle.rest.project;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.ProjectEvent;
import tech.artcoded.atriangle.api.dto.SinkResponse;
import tech.artcoded.atriangle.core.kafka.LoggerAction;

import javax.inject.Inject;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Listen to kafka output topic sinks, then update the project with the sink responses
 * Goal is to make the sink part totally asynchronous, while the rest part will still have some synchronous
 * part like the sparna conversion
 */
@Component
public class ProjectRestSinkConsumer {

  private final ObjectMapperWrapper objectMapperWrapper;
  private final LoggerAction loggerAction;
  private final ProjectRestService projectRestService;
  private final MongoTemplate mongoTemplate;

  @Inject
  public ProjectRestSinkConsumer(ObjectMapperWrapper objectMapperWrapper,
                                 LoggerAction loggerAction,
                                 ProjectRestService projectRestService,
                                 MongoTemplate mongoTemplate) {
    this.objectMapperWrapper = objectMapperWrapper;
    this.loggerAction = loggerAction;
    this.projectRestService = projectRestService;
    this.mongoTemplate = mongoTemplate;
  }


  @KafkaListener(topics = {"${event.dispatcher.elastic-sink-topic-out}",
                           "${event.dispatcher.mongodb-sink-topic-out}",
                           "${event.dispatcher.rdf-sink-topic-out}"})
  public void sink(ConsumerRecord<String, String> record) throws Exception {
    SinkResponse response = objectMapperWrapper.deserialize(record.value(), SinkResponse.class).orElseThrow();
    ProjectEvent projectEvent = projectRestService.findById(response.getCorrelationId()).orElseThrow();


    ProjectEvent newProjectEvent = projectEvent.toBuilder()
                                               .sinkResponses(Stream.concat(projectEvent.getSinkResponses()
                                                                                        .stream(), Stream.of(response))
                                                                    .collect(Collectors.toUnmodifiableList()))
                                               .build();
    ProjectEvent updatedProjectEvent = this.mongoTemplate.save(newProjectEvent);
    loggerAction.info(projectEvent::getId, "received sink response with status %s, for project %s", response.getSinkResponsestatus()
                                                                                                            .name(), updatedProjectEvent
                        .getId());
  }
}
