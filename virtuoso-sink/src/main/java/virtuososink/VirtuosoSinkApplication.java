package virtuososink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import tech.artcoded.atriangle.core.config.KafkaConfig;
import tech.artcoded.atriangle.core.config.VirtuosoConfig;

@SpringBootApplication
@EnableScheduling
@Import({VirtuosoConfig.class, KafkaConfig.class})
public class VirtuosoSinkApplication {
  public static void main(String[] args) {
    SpringApplication.run(VirtuosoSinkApplication.class, args);
  }
}
