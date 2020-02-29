package tech.artcoded.atriangle.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.PropertyStore;
import tech.artcoded.atriangle.api.kafka.CommonConstants;
import tech.artcoded.atriangle.api.kafka.KafkaHelper;
import tech.artcoded.atriangle.api.kafka.KafkaService;
import tech.artcoded.atriangle.api.kafka.SimpleKafkaTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Optional;

import static tech.artcoded.atriangle.core.config.NamedBean.*;

@Configuration
@Slf4j
public class KafkaConfig {

  @Bean
  @Named(OBJECT_MAPPER_WRAPPER)
  public ObjectMapperWrapper objectMapperWrapper() {
    return ObjectMapper::new;
  }

  @Bean
  @Named(KAFKA_TEMPLATE)
  public SimpleKafkaTemplate kafkaTemplate() {

    StringSerializer stringSerializer = new StringSerializer();
    StringDeserializer stringDeserializer = new StringDeserializer();

    PropertyStore producerConfig = PropertyStore.single(CommonConstants.KAFKA_PROPERTY_FILE);
    PropertyStore consumerConfig = PropertyStore.single(CommonConstants.CONSUMER_PROPERTY_FILE);

    var props = PropertyStore.systemProperties()
                             .merge(producerConfig)
                             .merge(consumerConfig);

    String toTopic = props.getRequiredPropertyAsString(CommonConstants.TOPIC_TO);
    List<String> fromTopic = props.getRequiredPropertyAsListOfString(CommonConstants.TOPIC_FROM, Optional.empty());

    Producer<String, String> producer = KafkaHelper.createProducer(producerConfig, stringSerializer.getClass(), stringSerializer.getClass());
    Consumer<String, String> consumer = KafkaHelper.createConsumer(consumerConfig, stringDeserializer.getClass(), stringDeserializer.getClass(), fromTopic);

    return SimpleKafkaTemplate.builder()
                              .topic(toTopic)
                              .producerConfig(producerConfig)
                              .consumerConfig(consumerConfig)
                              .producer(producer)
                              .consumer(consumer)
                              .serializerKey(stringSerializer)
                              .serializerValue(stringSerializer)
                              .deserializerKey(stringDeserializer)
                              .deserializerValue(stringDeserializer)
                              .build();
  }


  @Bean
  @Named(KAFKA_SERVICE)
  @Inject
  public KafkaService kafkaService(@Named(KAFKA_TEMPLATE) SimpleKafkaTemplate kafkaTemplate,
                                   @Named(OBJECT_MAPPER_WRAPPER) ObjectMapperWrapper objectMapperWrapper) {
    return new KafkaService(kafkaTemplate, objectMapperWrapper);
  }
}
