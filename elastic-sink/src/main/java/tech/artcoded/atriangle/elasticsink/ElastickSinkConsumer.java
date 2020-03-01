package tech.artcoded.atriangle.elasticsink;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.ElasticSearchRdfService;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.kafka.ElasticEvent;
import tech.artcoded.atriangle.api.kafka.KafkaEvent;
import tech.artcoded.atriangle.core.kafka.ATriangleConsumer;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class ElastickSinkConsumer implements ATriangleConsumer<String, String> {
  private final ElasticSearchRdfService elasticSearchRdfService;
  private final KafkaTemplate<String,String> kafkaTemplate;
  private final ObjectMapperWrapper mapperWrapper;


  @Value("${out.topic}")
  private String outTopic;

  @Inject
  public ElastickSinkConsumer(ElasticSearchRdfService elasticSearchRdfService,
                              KafkaTemplate<String, String> kafkaTemplate,
                              ObjectMapperWrapper objectMapperWrapper) {
    this.elasticSearchRdfService = elasticSearchRdfService;
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = objectMapperWrapper;
  }


  @Override
  public Map.Entry<String, String> consume(ConsumerRecord<String, String> record) {
    String elasticEvent = record.value();

    Optional<KafkaEvent> optionalKafkaEvent = mapperWrapper.deserialize(elasticEvent, KafkaEvent.class);
    KafkaEvent kafkaEvent = optionalKafkaEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));
    Optional<ElasticEvent> optionalElasticEvent = mapperWrapper.deserialize(kafkaEvent.getEvent(), ElasticEvent.class);
    ElasticEvent event = optionalElasticEvent.orElseThrow(() -> new RuntimeException("event could not be parsed"));

    String index = event.getIndex();

    if (event.isCreateIndex()) {
      log.info("index must be created");
      boolean indexExist = elasticSearchRdfService.indexExist(index);
      if (indexExist) {
        AcknowledgedResponse acknowledgedResponse = elasticSearchRdfService.deleteIndex(index);
        if (!acknowledgedResponse.isAcknowledged()) {
          log.info("acknowledge false");
          throw new RuntimeException("could not delete index");
        }
      }
      elasticSearchRdfService.createIndex(index, createIndexRequest -> createIndexRequest.settings(event.getSettings(), XContentType.JSON)
                                                                                         .mapping(event.getMappings(), XContentType.JSON));
      log.info("index created");

    }

    IndexResponse response = elasticSearchRdfService.index(index, kafkaEvent.getId(), kafkaEvent.getJson());
    log.info("status {}", response.status());

    if (!RestStatus.CREATED.equals(response.status())) {
      log.info("could not index");
      throw new RuntimeException("could not index");
    }
    log.info("index done {}");

    return Map.entry(UUID.randomUUID()
                         .toString(), mapperWrapper.serialize(Map.of("status", response.status()
                                                                                       .getStatus(),
                                                                     "id", kafkaEvent.getId())));
  }

  @Override
  @KafkaListener(topics = "${spring.kafka.template.default-topic}")
  public void sink(ConsumerRecord<String, String> record) throws Exception {
    log.info("receiving key {} value {}", record.key(), record.value());
    Map.Entry<String, String> response = consume(record);
    kafkaTemplate.send(new ProducerRecord<>(outTopic, response.getKey(), response.getValue()));
  }


}
