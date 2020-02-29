package tech.artcoded.atriangle.api.kafka;

public final class CommonConstants {
    public static final String ENABLE_KAFKA_CLIENT = "enableDefaultKafkaClient";
    public static final String KAFKA_PROPERTY_FILE = "kafka.properties";
    public static final String CONSUMER_PROPERTY_FILE = "consumer.properties";
    public static final String APP_PROPERTY_FILE = "application.properties";
    public static final String TOPIC_FROM = "topic.from";
    public static final String TOPIC_TO = "topic.to";
    public static final String DEFAULT_PROP_SEPARATOR = ",";

    public static final String ROUTE_PRODUCER_DEAD_LETTER_QUEUE = "{{route.producer.deadletter.queue}}";
    public static final String ROUTE_CONSUMER_DEAD_LETTER_QUEUE = "{{route.consumer.deadletter.queue}}";
    public static final String ROUTE_DEAD_LETTER_QUEUE_HANDLER_PRODUCER = "direct:produce-to-dlq-handler";

    public static final String SYS_CONFIG_FILE_PATH = "externalFilePath";
    public static final String PROPERTY_FILE_NAME = "propertyFileName";

    public static final String MANAGEMENT_REST_ROOT_PATH = "management.path";
    public static final String MANAGEMENT_REST_PORT = "management.port";
    public static final String MANAGEMENT_REST_HOST = "management.host";
    public static final String MANAGEMENT_ENABLED = "management.enabled";
    public static final String PROJECT_NAME = "projectName";
    public static final String PROJECT_VERSION = "projectVersion";

    public static final String DEAD_LETTER_MAX_MESSAGE = "dead.letter.max.message";
    public static final String DEAD_LETTER_QUEUE_REST_POLLING_TIMEOUT = "dead.letter.rest.polling.timeout";

    public static final String CAMEL_CTX_NAME = "camel.application.ctx.name";

    public static final String ROUTE_REPROCESS_INPUT_EVENT = "{{route.reprocess.input}}";
}
