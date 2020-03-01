package tech.artcoded.atriangle.elasticsink;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.ElasticSearchRdfService;
import tech.artcoded.atriangle.api.kafka.ElasticEvent;
import tech.artcoded.atriangle.core.kafka.ATriangleConsumer;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class ElastickSinkConsumer extends ATriangleConsumer<String, String> {
  private final ElasticSearchRdfService elasticSearchRdfService;

  @Inject
  public ElastickSinkConsumer(ElasticSearchRdfService elasticSearchRdfService) {
    this.elasticSearchRdfService = elasticSearchRdfService;
  }


  @Override
  public Map.Entry<String, String> consume(ConsumerRecord<String, String> record) {
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
    return Map.entry(UUID.randomUUID()
                         .toString(), mapperWrapper.serialize(Map.of("status", response.status()
                                                                                       .getStatus(),
                                                                     "id", event.getId())));
  }

}
