package tech.artcoded.atriangle.elasticsink;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.CheckedFunction;
import tech.artcoded.atriangle.api.IdGenerators;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.ElasticEvent;
import tech.artcoded.atriangle.api.dto.KafkaEvent;
import tech.artcoded.atriangle.core.elastic.ElasticSearchRdfService;
import tech.artcoded.atriangle.core.kafka.ATriangleConsumer;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class ElasticSinkConsumer implements ATriangleConsumer<String, String> {
  private final ElasticSearchRdfService elasticSearchRdfService;
  private final FileRestFeignClient fileRestFeignClient;
  @Getter
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapperWrapper mapperWrapper;


  @Value("${out.topic}")
  @Getter
  private String outTopic;

  @Inject
  public ElasticSinkConsumer(ElasticSearchRdfService elasticSearchRdfService,
                             FileRestFeignClient fileRestFeignClient,
                             KafkaTemplate<String, String> kafkaTemplate,
                             ObjectMapperWrapper objectMapperWrapper) {
    this.elasticSearchRdfService = elasticSearchRdfService;
    this.fileRestFeignClient = fileRestFeignClient;
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = objectMapperWrapper;
  }


  @Override
  public Map<String, String> consume(ConsumerRecord<String, String> record) throws Exception {
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

      ResponseEntity<ByteArrayResource> settings = fileRestFeignClient.download(event.getSettings().getId());
      ResponseEntity<ByteArrayResource> mappings = fileRestFeignClient.download(event.getMappings().getId());

      CheckedFunction<HttpEntity<ByteArrayResource>, String> inputStreamToString = entity -> IOUtils.toString(entity.getBody()
                                                                                                                    .getInputStream(), StandardCharsets.UTF_8);

      CreateIndexResponse response = elasticSearchRdfService.createIndex(index, createIndexRequest -> createIndexRequest.settings(inputStreamToString
                                                                                                                                    .safeApply(settings), XContentType.JSON)
                                                                                                                        .mapping(inputStreamToString
                                                                                                                                   .safeApply(mappings), XContentType.JSON));
      log.info("acknowledge of index creation {}", response.isAcknowledged());

    }

    ResponseEntity<ByteArrayResource> inputToSink = fileRestFeignClient.download(kafkaEvent.getInputToSink().getId());

    IndexResponse response = elasticSearchRdfService.index(index, kafkaEvent.getId(), IOUtils.toString(inputToSink.getBody()
                                                                                                                  .getInputStream(), StandardCharsets.UTF_8));

    log.info("status {}", response.status());

    if (!RestStatus.CREATED.equals(response.status())) {
      log.info("could not index");
      throw new RuntimeException("could not index");
    }

    return Map.of(IdGenerators.get(), String.format("indexing for kafka event id %s has result: status '%s', result '%s'", kafkaEvent
      .getId(), response.status().getStatus(), response.toString()));
  }


}
