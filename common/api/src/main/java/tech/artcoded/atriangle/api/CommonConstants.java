package tech.artcoded.atriangle.api;

public interface CommonConstants {
  String KAFKA_PROPERTY_FILE = "kafka.properties";
  String CONSUMER_PROPERTY_FILE = "consumer.properties";
  String TOPIC_FROM = "topic.from";
  String TOPIC_TO = "topic.to";
  String DEFAULT_PROP_SEPARATOR = ",";
  String DEFAULT_TOPIC = "username";

  String SYS_CONFIG_FILE_PATH = "externalFilePath";
  String PROPERTY_FILE_NAME = "propertyFileName";

  String DEAD_LETTER_MAX_MESSAGE = "dead.letter.max.message";
  String DEAD_LETTER_QUEUE_REST_POLLING_TIMEOUT = "dead.letter.rest.polling.timeout";

  String XLSX_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
}
