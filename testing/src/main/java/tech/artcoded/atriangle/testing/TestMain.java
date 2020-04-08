package tech.artcoded.atriangle.testing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class TestMain {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public TestingUtils testingUtils() {
    return this::restTemplate;
  }


  public static void main(String[] args) {
    SpringApplication.run(TestMain.class);
  }
}
