package tech.artcoded.atriangle.rest.user;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@ConfigurationProperties("fixture")
public class UserFixture implements CommandLineRunner {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  @Getter @Setter private List<User> users;

  @Inject
  public UserFixture(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) throws Exception {
    userRepository.deleteAll();
    userRepository.saveAll(
        users.stream()
            .peek(user -> log.info("saving user {}", user.getUsername()))
            .map(
                user ->
                    user.toBuilder().password(passwordEncoder.encode(user.getPassword())).build())
            .collect(Collectors.toList()));
  }
}
