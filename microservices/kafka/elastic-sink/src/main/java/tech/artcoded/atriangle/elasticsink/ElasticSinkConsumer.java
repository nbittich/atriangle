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
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.CheckedFunction;
import tech.artcoded.atriangle.api.CheckedSupplier;
import tech.artcoded.atriangle.api.IdGenerators;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.*;
import tech.artcoded.atriangle.core.elastic.ElasticSearchRdfService;
import tech.artcoded.atriangle.core.kafka.KafkaEventHelper;
import tech.artcoded.atriangle.core.kafka.KafkaSink;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ElasticSinkConsumer implements KafkaSink<String, String> {
  private final ElasticSearchRdfService elasticSearchRdfService;
  private final FileRestFeignClient fileRestFeignClient;
  private final KafkaEventHelper kafkaEventHelper;
  @Getter
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapperWrapper mapperWrapper;
  private final BuildProperties buildProperties;



  @Value("${kafka.dispatcher.elastic-sink-topic-out")
  private String outTopic;

  @Inject
  public ElasticSinkConsumer(ElasticSearchRdfService elasticSearchRdfService,
                             FileRestFeignClient fileRestFeignClient,
                             KafkaEventHelper kafkaEventHelper,
                             KafkaTemplate<String, String> kafkaTemplate,
                             ObjectMapperWrapper objectMapperWrapper,
                             BuildProperties buildProperties) {
    this.elasticSearchRdfService = elasticSearchRdfService;
    this.fileRestFeignClient = fileRestFeignClient;
    this.kafkaEventHelper = kafkaEventHelper;
    this.kafkaTemplate = kafkaTemplate;
    this.mapperWrapper = objectMapperWrapper;
    this.buildProperties = buildProperties;
  }


  @Override
  public List<KafkaMessage<String, String>> consume(ConsumerRecord<String, String> record) throws Exception {
    CheckedFunction<HttpEntity<ByteArrayResource>, String> inputStreamToString = entity -> entity == null || entity.getBody() == null ? null : IOUtils
      .toString(entity.getBody()
                      .getInputStream(), StandardCharsets.UTF_8);

    String elasticEvent = record.value();

    KafkaEvent kafkaEvent = kafkaEventHelper.parseKafkaEvent(elasticEvent);
    ElasticEvent event = kafkaEventHelper.parseEvent(kafkaEvent, ElasticEvent.class);

    String index = event.getIndex();

    log.info("index must be created");
    boolean indexExist = elasticSearchRdfService.indexExist(index);

    if (!indexExist) {
      AcknowledgedResponse acknowledgedResponse = elasticSearchRdfService.deleteIndex(index);
      if (!acknowledgedResponse.isAcknowledged()) {
        log.info("acknowledge false");
        throw new RuntimeException("could not delete index");
      }

      ResponseEntity<ByteArrayResource> settings = fileRestFeignClient.download(event.getSettings()
                                                                                     .getId());
      ResponseEntity<ByteArrayResource> mappings = fileRestFeignClient.download(event.getMappings()
                                                                                     .getId());


      CreateIndexResponse response = elasticSearchRdfService.createIndex(index, createIndexRequest -> createIndexRequest.settings(inputStreamToString
                                                                                                                                    .safeApply(settings), XContentType.JSON)
                                                                                                                        .mapping(inputStreamToString
                                                                                                                                   .safeApply(mappings), XContentType.JSON));
      log.info("acknowledge of index creation {}", response.isAcknowledged());

    }

    ResponseEntity<ByteArrayResource> inputToSink = fileRestFeignClient.download(kafkaEvent.getInputToSink()
                                                                                           .getId());

    IndexResponse response = elasticSearchRdfService.index(index, kafkaEvent.getId(), IOUtils.toString(inputToSink.getBody()
                                                                                                                  .getInputStream(), StandardCharsets.UTF_8));

    log.info("status {}", response.status());

    if (!RestStatus.CREATED.equals(response.status())) {
      log.info("could not index");
      throw new RuntimeException("could not index");
    }

    SinkResponse sinkResponse = SinkResponse.builder()
                                            .sinkResponsestatus(SinkResponse.SinkResponseStatus.SUCCESS)
                                            .finishedDate(new Date())
                                            .response("rdf saved to the elastic search instance".getBytes())
                                            .responseType(EventType.ELASTIC_SINK_OUT)
                                            .build();//todo think about failure..


    KafkaEvent kafkaEventForSinkOut = kafkaEventHelper.newKafkaEventBuilder(kafkaEvent.getCorrelationId(), buildProperties)
                                                      .id(IdGenerators.get())
                                                      .eventType(EventType.ELASTIC_SINK_OUT)
                                                      .event(mapperWrapper.serialize(sinkResponse))
                                                      .build();

    CheckedSupplier<KafkaMessage.KafkaMessageBuilder<String,String>> builder = KafkaMessage::builder;

    return List.of(builder.safeGet().key(IdGenerators.get()).value(mapperWrapper.serialize(kafkaEventForSinkOut)).outTopic(outTopic).build());

  }


}
