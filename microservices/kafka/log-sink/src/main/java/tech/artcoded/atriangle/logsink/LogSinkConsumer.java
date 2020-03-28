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
import tech.artcoded.atriangle.api.dto.KafkaMessage;
import tech.artcoded.atriangle.api.dto.LogEvent;
import tech.artcoded.atriangle.core.elastic.ElasticSearchRdfService;
import tech.artcoded.atriangle.core.kafka.KafkaEventHelper;
import tech.artcoded.atriangle.core.kafka.KafkaSink;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class LogSinkConsumer implements KafkaSink<String, String> {
  private final ElasticSearchRdfService elasticSearchRdfService;
  @Getter
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapperWrapper mapperWrapper;
  private final KafkaEventHelper kafkaEventHelper;

  @Value("${log.sink.index}")
  private String logSinkIndex;


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
                         ObjectMapperWrapper objectMapperWrapper,
                         KafkaEventHelper kafkaEventHelper) {
    this.elasticSearchRdfService = elasticSearchRdfService;
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = objectMapperWrapper;
    this.kafkaEventHelper = kafkaEventHelper;
  }


  @Override
  public List<KafkaMessage<String, String>> consume(ConsumerRecord<String, String> record) {
    String logEvent = record.value();

    LogEvent event = kafkaEventHelper.parseEvent(logEvent, LogEvent.class);

    String uuid = UUID.randomUUID()
                      .toString();

    IndexResponse response = elasticSearchRdfService.index(logSinkIndex, uuid, mapperWrapper.serialize(event));

    log.info("status {}", response.status());

    if (!RestStatus.CREATED.equals(response.status())) {
      log.info("could not index");
      throw new RuntimeException("could not index");
    }

    return List.of(); // no need to do anything
  }


}
