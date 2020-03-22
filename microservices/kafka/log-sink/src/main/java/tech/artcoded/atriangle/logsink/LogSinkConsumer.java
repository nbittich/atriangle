package tech.artcoded.atriangle.logsink;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.LogEvent;
import tech.artcoded.atriangle.core.elastic.ElasticSearchRdfService;
import tech.artcoded.atriangle.core.kafka.ATriangleConsumer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class LogSinkConsumer implements ATriangleConsumer<String, String> {
  private final ElasticSearchRdfService elasticSearchRdfService;
  @Getter
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapperWrapper mapperWrapper;

  @Value("${log.sink.index}")
  private String logSinkIndex;

  @Value("${out.topic}")
  @Getter
  private String outTopic;

  @PostConstruct
  public void createIndexIfNotExist() {
    boolean indexExist = elasticSearchRdfService.indexExist(logSinkIndex);
    if (!indexExist) {
      CreateIndexResponse response = elasticSearchRdfService.createIndex(logSinkIndex, createIndexRequest -> createIndexRequest);
      log.info("acknowledge of index creation {}", response.isAcknowledged());
    }
  }

  @Inject
  public LogSinkConsumer(ElasticSearchRdfService elasticSearchRdfService,
                         KafkaTemplate<String, String> kafkaTemplate,
                         ObjectMapperWrapper objectMapperWrapper) {
    this.elasticSearchRdfService = elasticSearchRdfService;
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = objectMapperWrapper;
  }


  @Override
  public Map<String, String> consume(ConsumerRecord<String, String> record) {
    String logEvent = record.value();

    Optional<LogEvent> optionalElasticEvent = mapperWrapper.deserialize(logEvent, LogEvent.class);
    LogEvent event = optionalElasticEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));


    String uuid = UUID.randomUUID()
                      .toString();

    IndexResponse response = elasticSearchRdfService.index(logSinkIndex, uuid, mapperWrapper.serialize(event));

    log.info("status {}", response.status());

    if (!RestStatus.CREATED.equals(response.status())) {
      log.info("could not index");
      throw new RuntimeException("could not index");
    }

    return Map.of(uuid, mapperWrapper.serialize(Map.of("message", "indexing success",
                                                       "status", response.status()
                                                                         .getStatus(),
                                                       "id", uuid)));
  }


}
