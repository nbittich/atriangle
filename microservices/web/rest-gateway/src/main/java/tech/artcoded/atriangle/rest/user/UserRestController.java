package tech.artcoded.atriangle.rest.user;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.security.Principal;


@RestController
@RequestMapping("/api/user")
public class UserRestController {
  private final UserRepository userRepository;

  @Inject
  public UserRestController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @PostMapping("/info")
  public User info(Principal principal) {
    return userRepository.principalToUser(principal);
  }

}
