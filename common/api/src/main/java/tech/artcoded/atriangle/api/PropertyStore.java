package tech.artcoded.atriangle.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface PropertyStore {
  Function<String, InputStream> getClasspathResource =
      PropertyStore.class.getClassLoader()::getResourceAsStream;
  Logger log = LoggerFactory.getLogger("IPropertiesHelper");
  String SYSTEM_PREFIX = "[SYSTEM]";

  static PropertyStore empty() {
    return Collections::emptyMap;
  }

  static PropertyStore fromProperties(Properties props) {
    final Map<Object, Object> properties =
        props.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o1, o2) -> o2));
    return () -> properties;
  }

  Map<Object, Object> getProperties();

  default void setProperty(String propertyName, Object value) {
    this.getProperties().put(propertyName, value);
  }

  default Map<Object, Object> getPropertiesFilterSystemProperties() {
    return getProperties().entrySet().stream()
        .filter(e -> !e.getKey().toString().contains(SYSTEM_PREFIX))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  default Properties toProperties() {
    Properties properties = new Properties();
    properties.putAll(getProperties());
    return properties;
  }

  static Properties load(InputStream is) {
    try (InputStream openIs = is) {
      Properties properties = new Properties();
      properties.load(openIs);
      return properties;
    } catch (Exception e) {
      log.info("an error occured when loading properties", e);
      throw new RuntimeException(
          "error while loading properties file.Check that the file exist.", e);
    }
  }

  static PropertyStore systemProperties() {
    final Map<Object, Object> systemProperties =
        Stream.concat(
                System.getProperties().entrySet().stream(), System.getenv().entrySet().stream())
            .map(entry -> Map.entry(SYSTEM_PREFIX + entry.getKey().toString(), entry.getValue()))
            .distinct()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return () -> systemProperties;
  }

  static PropertyStore withSystemProperties(String... locations) {
    final Stream<Map.Entry<Object, Object>> propertiesFromFiles =
        Stream.of(locations)
            .map(getClasspathResource)
            .map((PropertyStore::load))
            .map(Properties::entrySet)
            .flatMap(Collection::stream);

    final Stream<Map.Entry<Object, Object>> systemProperties =
        systemProperties().getProperties().entrySet().stream();

    final Map<Object, Object> collect =
        Stream.concat(propertiesFromFiles, systemProperties)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o1, o2) -> o2));

    return () -> collect;
  }

  static PropertyStore single(String location) {
    Properties properties = load(getClasspathResource.apply(location));
    return () ->
        properties.entrySet().stream()
            .distinct()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o1, o2) -> o2));
  }

  static PropertyStore single(InputStream is) {
    final Properties properties = PropertyStore.load(is);
    final Map<Object, Object> collect =
        properties.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return () -> collect;
  }

  static PropertyStore mergeAll(PropertyStore... helpers) {
    Map<Object, Object> collect =
        Stream.of(helpers)
            .map(PropertyStore::getProperties)
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o1, o2) -> o2));
    return () -> collect;
  }

  static PropertyStore reduce(PropertyStore helper1, PropertyStore helper2) {
    return mergeAll(helper1, helper2);
  }

  default PropertyStore merge(PropertyStore helper) {
    return mergeAll(this, helper);
  }

  default Optional<String> getPropertyAsString(String key) {
    return Optional.ofNullable(getProperties().get(key)).map(String::valueOf);
  }

  default List<String> getRequiredPropertyAsListOfString(String key, Optional<String> separator) {
    String prop = getRequiredPropertyAsString(key);
    String[] properties = prop.split(separator.orElse(CommonConstants.DEFAULT_PROP_SEPARATOR));
    return Arrays.asList(properties);
  }

  default String getRequiredPropertyAsString(String key) {
    return getPropertyAsString(key).orElseThrow(() -> new RuntimeException(key + " not found"));
  }

  default Optional<Boolean> getPropertyAsBoolean(String key) {
    return getPropertyAsString(key).map(Boolean::valueOf);
  }

  default Optional<Integer> getPropertyAsInteger(String key) {
    return getPropertyAsString(key).map(Integer::valueOf);
  }

  default Integer getRequiredPropertyAsInteger(String key) {
    return getPropertyAsInteger(key).orElseThrow(() -> new RuntimeException(key + " not found"));
  }

  default <T> T getPropertyAs(String key, Function<String, T> transformer) {
    return getPropertyAsString(key)
        .map(transformer)
        .orElseThrow(() -> new RuntimeException(key + " not found"));
  }

  default Optional<Long> getPropertyAsLong(String key) {
    return getPropertyAsString(key).map(Long::valueOf);
  }

  default Long getRequiredPropertyAsLong(String key) {
    return getPropertyAsLong(key).orElseThrow(() -> new RuntimeException(key + " not found"));
  }

  default String getPropertyAsString(String key, String orElse) {
    return getPropertyAsString(key).orElse(orElse);
  }

  default Boolean getPropertyAsBoolean(String key, Boolean orElse) {
    return getPropertyAsBoolean(key).orElse(orElse);
  }

  default Integer getPropertyAsInteger(String key, Integer orElse) {
    return getPropertyAsInteger(key).orElse(orElse);
  }

  default Long getPropertyAsLong(String key, Long orElse) {
    return getPropertyAsString(key).map(Long::valueOf).orElse(orElse);
  }

  default void ifPropertyPresent(String key, Consumer<String> consumer) {
    Optional<String> prop = getPropertyAsString(key);
    prop.ifPresent(consumer);
  }
}
