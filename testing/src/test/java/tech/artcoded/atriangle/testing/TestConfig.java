package tech.artcoded.atriangle.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.inject.Inject;

@Configuration
public class TestConfig {

  private final Environment env;

  @Inject
  public TestConfig(Environment env) {
    this.env = env;
  }

  @Bean
  public TestRestTemplate restTemplate() {
    return new TestRestTemplate(
        env.getRequiredProperty("backend.credentials.username"),
        env.getRequiredProperty("backend.credentials.password"));
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public TestingUtils testingUtils() {
    return this::restTemplate;
  }
}
