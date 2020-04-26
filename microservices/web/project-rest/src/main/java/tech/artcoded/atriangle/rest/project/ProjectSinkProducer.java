package tech.artcoded.atriangle.rest.project;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.IdGenerators;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.*;
import tech.artcoded.atriangle.core.kafka.KafkaEventHelper;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

import static tech.artcoded.atriangle.api.CommonConstants.DEFAULT_TOPIC;

@Component
@Slf4j
public class ProjectSinkProducer {

  private final ProjectFileService projectFileService;
  private final ProjectService projectService;
  private final ObjectMapperWrapper objectMapperWrapper;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final BuildProperties buildProperties;
  private final KafkaEventHelper kafkaEventHelper;

  @Value(DEFAULT_TOPIC)
  private String topicProducer;

  @Inject
  public ProjectSinkProducer(ProjectFileService projectFileService,
                             ProjectService projectService,
                             ObjectMapperWrapper objectMapperWrapper,
                             KafkaTemplate<String, String> kafkaTemplate,
                             BuildProperties buildProperties,
                             KafkaEventHelper kafkaEventHelper) {
    this.projectFileService = projectFileService;
    this.projectService = projectService;
    this.objectMapperWrapper = objectMapperWrapper;
    this.kafkaTemplate = kafkaTemplate;
    this.buildProperties = buildProperties;
    this.kafkaEventHelper = kafkaEventHelper;
  }

  public void sink(SinkRequest sinkRequest) {
    CompletableFuture.runAsync(() -> {
      String projectId = sinkRequest.getProjectId();
      log.info("sink {}, request {}", projectId, sinkRequest.getRdfFileEventId());
      ProjectEvent projectEvent = projectService.findById(projectId)
                                                .orElseThrow();
      String ns = projectEvent.getName();


      RestEvent restRdfEvent = RestEvent.builder()
                                        .namespace(ns)
                                        .inputToSink(projectFileService.getFileMetadata(projectId, sinkRequest.getRdfFileEventId())
                                                                       .orElseThrow())
                                        .shaclModel(projectFileService.getFileMetadata(projectId, sinkRequest.getShaclFileEventId())
                                                                      .orElse(null))
                                        .build();

      KafkaEvent kafkaEvent = kafkaEventHelper.newKafkaEventBuilderWithoutRecord(projectId,
                                                                                 buildProperties)
                                              .eventType(EventType.RDF_SINK)
                                              .id(IdGenerators.get())
                                              .event(objectMapperWrapper.serialize(restRdfEvent))
                                              .build();

      log.info("sending kafka event");

      ProducerRecord<String, String> restRecord = new ProducerRecord<>(topicProducer, kafkaEvent.getId(), objectMapperWrapper.serialize(kafkaEvent));

      kafkaTemplate.send(restRecord);
    });

  }
}
