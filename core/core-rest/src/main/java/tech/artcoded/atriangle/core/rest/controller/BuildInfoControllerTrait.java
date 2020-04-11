package tech.artcoded.atriangle.core.rest.controller;

import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import tech.artcoded.atriangle.core.rest.annotation.SwaggerHeaderAuthentication;

public interface BuildInfoControllerTrait {
  BuildProperties getBuildProperties();

  @GetMapping("/build-info")
  @SwaggerHeaderAuthentication
  default BuildProperties getProperties() {
    return getBuildProperties();
  }
}
