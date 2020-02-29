package tech.artcoded.atriangle.elasticsink;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.ElasticSearchRdfService;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.kafka.ElasticEvent;
import tech.artcoded.atriangle.api.kafka.SimpleKafkaTemplate;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class ElastickSinkCron {
  private final ElasticSearchRdfService elasticSearchRdfService;
  private final ObjectMapperWrapper mapperWrapper;
  private final SimpleKafkaTemplate kafkaTemplate;

  @Value("classpath:elastic-index-config.json")
  private Resource elasticSourceConfig;


  @Inject
  public ElastickSinkCron(ElasticSearchRdfService elasticSearchRdfService,
                          ObjectMapperWrapper mapperWrapper,
                          SimpleKafkaTemplate kafkaTemplate) {
    this.elasticSearchRdfService = elasticSearchRdfService;
    this.mapperWrapper = mapperWrapper;
    this.kafkaTemplate = kafkaTemplate;
  }

  @Scheduled(cron = "${elasticsink.cron}")
  public void sink() {
    log.info("sink started...");
    kafkaTemplate.consume(300L, ChronoUnit.MILLIS, consumerRecords -> {
      consumerRecords.forEach(record -> {
        String elasticEvent = record.value();
        Optional<ElasticEvent> optionalElasticEvent = mapperWrapper.deserialize(elasticEvent, ElasticEvent.class);
        ElasticEvent event = optionalElasticEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));
        String index = event.getIndex();

        if (event.isCreateIndex()) {
          boolean indexExist = elasticSearchRdfService.indexExist(index);
          if (indexExist) {
            AcknowledgedResponse acknowledgedResponse = elasticSearchRdfService.deleteIndex(index);
            if (!acknowledgedResponse.isAcknowledged()) {
              throw new RuntimeException("could not delete index");
            }
          }
          elasticSearchRdfService.createIndex(index, createIndexRequest -> createIndexRequest.settings(event.getSettings(), XContentType.JSON)
                                                                                             .mapping(event.getMappings(), XContentType.JSON));
        }

        IndexResponse response = elasticSearchRdfService.index(index, event.getId(), event.getJson());
        kafkaTemplate.produce(UUID.randomUUID()
                                  .toString(), mapperWrapper.serialize(Map.of("status", response.status()
                                                                                                .getStatus(),
                                                                              "id", event.getId())));
      });
    });
    log.info("sink ended...");

  }
}
