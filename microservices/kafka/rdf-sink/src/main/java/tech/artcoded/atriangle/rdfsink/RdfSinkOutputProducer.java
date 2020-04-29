package tech.artcoded.atriangle.rdfsink;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;
import tech.artcoded.atriangle.api.CheckedSupplier;
import tech.artcoded.atriangle.api.IdGenerators;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.*;
import tech.artcoded.atriangle.core.kafka.KafkaEventHelper;
import tech.artcoded.atriangle.core.kafka.LoggerAction;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * in case rdf sink is successfull, we produce new events to sink into mongodb/elastic this could
 * also be done in the project layer
 */
@Component
public class RdfSinkOutputProducer {

  private final ObjectMapperWrapper mapperWrapper;
  private final BuildProperties buildProperties;
  private final KafkaEventHelper kafkaEventHelper;
  private final LoggerAction loggerAction;

  @Value("${kafka.dispatcher.mongodb-sink-topic}")
  private String mongoSinkTopic;

  @Inject
  public RdfSinkOutputProducer(
      ObjectMapperWrapper mapperWrapper,
      BuildProperties buildProperties,
      KafkaEventHelper kafkaEventHelper,
      LoggerAction loggerAction) {
    this.mapperWrapper = mapperWrapper;
    this.buildProperties = buildProperties;
    this.kafkaEventHelper = kafkaEventHelper;
    this.loggerAction = loggerAction;
  }

  public List<KafkaMessage<String, String>> produce(
      KafkaEvent kafkaEvent,
      RestEvent event,
      FileEvent jsonLdFile,
      int partition,
      long offset,
      Headers headers) {

    KafkaEvent.KafkaEventBuilder kafkaEventBuilder =
        kafkaEventHelper.newKafkaEventBuilder(
            kafkaEvent.getCorrelationId(), partition, offset, headers, buildProperties);

    String mongoSinkEventId = IdGenerators.get();

    MongoEvent mongoEvent =
        MongoEvent.builder().collection(event.getNamespace()).inputToSink(jsonLdFile).build();
    String kafkaEventForMongo =
        mapperWrapper.serialize(
            kafkaEventBuilder
                .id(mongoSinkEventId)
                .eventType(EventType.MONGODB_SINK)
                .event(mapperWrapper.serialize(mongoEvent))
                .build());

    CheckedSupplier<KafkaMessage.KafkaMessageBuilder<String, String>> builder =
        KafkaMessage::builder;

    loggerAction.info(kafkaEvent::getCorrelationId, "rdf saved to triplestore");

    return Stream.of(
            builder
                .safeGet()
                .outTopic(mongoSinkTopic)
                .key(mongoSinkEventId)
                .value(kafkaEventForMongo)
                .build())
        .filter(m -> StringUtils.isNotBlank(m.getValue()))
        .collect(Collectors.toList());
  }
}
