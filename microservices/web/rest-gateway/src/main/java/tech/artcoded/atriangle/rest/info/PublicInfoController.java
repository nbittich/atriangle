package tech.artcoded.atriangle.rest.info;

import lombok.Getter;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.RestController;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.controller.BuildInfoControllerTrait;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;

import javax.inject.Inject;

@CrossOriginRestController
@RestController("/api/public")
public class PublicInfoController implements PingControllerTrait, BuildInfoControllerTrait {
  @Getter
  private final BuildProperties buildProperties;

  @Inject
  public PublicInfoController(BuildProperties buildProperties) {
    this.buildProperties = buildProperties;
  }
}
