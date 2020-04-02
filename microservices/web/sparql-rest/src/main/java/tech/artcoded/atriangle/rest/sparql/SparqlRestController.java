package tech.artcoded.atriangle.rest.sparql;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.info.BuildProperties;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.controller.BuildInfoControllerTrait;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.core.sparql.SimpleSparqlService;

import javax.inject.Inject;

@CrossOriginRestController
@Slf4j
public class SparqlRestController implements PingControllerTrait, BuildInfoControllerTrait {
  @Getter
  private final BuildProperties buildProperties;

  private final SimpleSparqlService simpleSparqlService;

  @Inject
  public SparqlRestController(BuildProperties buildProperties,
                              SimpleSparqlService simpleSparqlService) {
    this.buildProperties = buildProperties;
    this.simpleSparqlService = simpleSparqlService;
  }
  
}
