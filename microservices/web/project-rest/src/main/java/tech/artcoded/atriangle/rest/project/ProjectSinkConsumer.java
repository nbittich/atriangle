package tech.artcoded.atriangle.rest.project;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.FileEvent;
import tech.artcoded.atriangle.api.dto.FileEventType;
import tech.artcoded.atriangle.api.dto.KafkaEvent;
import tech.artcoded.atriangle.api.dto.ProjectEvent;
import tech.artcoded.atriangle.api.dto.SinkResponse;
import tech.artcoded.atriangle.core.kafka.KafkaEventHelper;
import tech.artcoded.atriangle.core.kafka.LoggerAction;

import javax.inject.Inject;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ProjectSinkConsumer {

  private final ObjectMapperWrapper objectMapperWrapper;
  private final KafkaEventHelper kafkaEventHelper;
  private final LoggerAction loggerAction;
  private final ProjectRestService projectRestService;
  private final MongoTemplate mongoTemplate;

  @Inject
  public ProjectSinkConsumer(ObjectMapperWrapper objectMapperWrapper,
                             KafkaEventHelper kafkaEventHelper, LoggerAction loggerAction,
                             ProjectRestService projectRestService,
                             MongoTemplate mongoTemplate) {
    this.objectMapperWrapper = objectMapperWrapper;
    this.kafkaEventHelper = kafkaEventHelper;
    this.loggerAction = loggerAction;
    this.projectRestService = projectRestService;
    this.mongoTemplate = mongoTemplate;
  }


  @KafkaListener(topics = {"${event.dispatcher.elastic-sink-topic-out}",
                           "${event.dispatcher.mongodb-sink-topic-out}"})
  public void sink(ConsumerRecord<String, String> record) throws Exception {

    KafkaEvent kafkaEvent = kafkaEventHelper.parseKafkaEvent(record.value());
    SinkResponse response = kafkaEventHelper.parseEvent(kafkaEvent, SinkResponse.class);

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

  @KafkaListener(topics = {"${event.dispatcher.rdf-sink-topic-out}"})
  public void addJsonLdFile(ConsumerRecord<String, String> record) throws Exception {
    KafkaEvent kafkaEvent = kafkaEventHelper.parseKafkaEvent(record.value());
    SinkResponse response = kafkaEventHelper.parseEvent(kafkaEvent, SinkResponse.class);

    FileEvent jsonLdFileEvent = objectMapperWrapper.deserialize(response.responseAsString(), FileEvent.class).orElseThrow();

    ProjectEvent projectEvent = projectRestService.findById(response.getCorrelationId()).orElseThrow();


    ProjectEvent newProjectEvent = projectEvent.toBuilder()
                                               .fileEvents(Stream.concat(projectEvent.getFileEvents()
                                                                                     .stream(), Stream.of(jsonLdFileEvent.toBuilder()
                                                                                                                         .eventType(FileEventType.PROJECT_FILE)
                                                                                                                         .build()))
                                                                 .collect(Collectors.toUnmodifiableList()))
                                               .build();
    ProjectEvent updatedProjectEvent = this.mongoTemplate.save(newProjectEvent);

    loggerAction.info(projectEvent::getId, "received jsonld file event  for project %s, mongo id: %s", projectEvent.getName(), updatedProjectEvent
      .getId());
  }
}
