package tech.artcoded.atriangle.rest.project;

import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.IdGenerators;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.EventType;
import tech.artcoded.atriangle.api.dto.KafkaEvent;
import tech.artcoded.atriangle.api.dto.ProjectEvent;
import tech.artcoded.atriangle.api.dto.RestEvent;
import tech.artcoded.atriangle.api.dto.SinkRequest;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class ProjectSinkProducer {

  private final ProjectRestService projectRestService;
  private final ObjectMapperWrapper objectMapperWrapper;
  private final KafkaTemplate<String, String> kafkaTemplate;

  @Value("${spring.kafka.template.default-topic}")
  private String topicProducer;

  @Inject
  public ProjectSinkProducer(ProjectRestService projectRestService,
                             ObjectMapperWrapper objectMapperWrapper,
                             KafkaTemplate<String, String> kafkaTemplate) {
    this.projectRestService = projectRestService;
    this.objectMapperWrapper = objectMapperWrapper;
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sink(SinkRequest sinkRequest) {
    CompletableFuture.runAsync(() -> {
      String projectId = sinkRequest.getProjectId();
      ProjectEvent projectEvent = projectRestService.findById(projectId).orElseThrow();
      String ns = Optional.ofNullable(sinkRequest.getNamespace())
                          .filter(StringUtils::isNotEmpty)
                          .orElseGet(projectEvent::getName);

      RestEvent restRdfEvent = RestEvent.builder()
                                        .namespace(ns)
                                        .elasticIndex(ns)
                                        .sinkToElastic(sinkRequest.isSinkToElastic())
                                        .elasticSettingsJson(projectRestService.getFileMetadata(projectId, sinkRequest.getElasticSettingsFileEventId())
                                                                               .orElse(null))
                                        .elasticMappingsJson(projectRestService.getFileMetadata(projectId, sinkRequest.getElasticMappingsFileEventId())
                                                                               .orElse(null))
                                        .build();

      KafkaEvent kafkaEvent = KafkaEvent.builder()
                                        .eventType(EventType.RDF_SINK)
                                        .correlationId(projectEvent.getId())
                                        .id(IdGenerators.get())
                                        .shaclModel(projectRestService.getFileMetadata(projectId, sinkRequest.getShaclFileEventId())
                                                                      .orElse(null))
                                        .inputToSink(projectRestService.getFileMetadata(projectId, sinkRequest.getRdfFileEventId())
                                                                       .orElseThrow())
                                        .event(objectMapperWrapper.serialize(restRdfEvent))
                                        .build();

      ProducerRecord<String, String> restRecord = new ProducerRecord<>(topicProducer, kafkaEvent.getId(), objectMapperWrapper.serialize(kafkaEvent));

      kafkaTemplate.send(restRecord);
    });

  }
}
