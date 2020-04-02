package tech.artcoded.atriangle.rest.sparql;


import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.openrdf.model.Model;
import org.openrdf.rio.RDFFormat;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tech.artcoded.atriangle.api.IdGenerators;
import tech.artcoded.atriangle.api.dto.FileEvent;
import tech.artcoded.atriangle.api.dto.RdfType;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.controller.BuildInfoControllerTrait;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.core.sparql.ModelConverter;
import tech.artcoded.atriangle.core.sparql.SimpleSparqlService;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;
import tech.artcoded.atriangle.feign.clients.sparql.SparqlRestFeignClient;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@CrossOriginRestController
@Slf4j
public class SparqlRestController implements PingControllerTrait, BuildInfoControllerTrait, SparqlRestFeignClient {
  @Getter
  private final BuildProperties buildProperties;

  private final SimpleSparqlService simpleSparqlService;

  private final FileRestFeignClient fileRestFeignClient;

  @Inject
  public SparqlRestController(BuildProperties buildProperties,
                              SimpleSparqlService simpleSparqlService,
                              FileRestFeignClient fileRestFeignClient) {
    this.buildProperties = buildProperties;
    this.simpleSparqlService = simpleSparqlService;
    this.fileRestFeignClient = fileRestFeignClient;
  }

  @Override
  public ResponseEntity<String> createNamespace(String namespace) {
    simpleSparqlService.createNamespace(namespace);
    return ResponseEntity.ok(String.format("namespace %s created", namespace));
  }

  @Override
  @SneakyThrows
  public ResponseEntity<String> loadRdfFile(String rdfFileEvent, String namespace) {
    FileEvent rdfFile = fileRestFeignClient.findById(rdfFileEvent).getBody();
    ResponseEntity<ByteArrayResource> rdf = fileRestFeignClient.download(rdfFile.getId(), IdGenerators.get());

    simpleSparqlService.load(namespace, rdf.getBody().getInputStream(), RDFFormat.forFileName(rdfFile.getName()));
    return ResponseEntity.ok("rdf loaded");
  }

  @Override
  public ResponseEntity<String> insertRdfAsJsonLd(String jsonLdModel, String namespace) {
    simpleSparqlService.load(namespace, IOUtils.toInputStream(jsonLdModel, StandardCharsets.UTF_8), RDFFormat.JSONLD);
    return ResponseEntity.ok("jsonLd loaded");

  }

  @Override
  public ResponseEntity<String> convert(String jsonLdModel, RdfType rdfFormat) {
    RDFFormat format = RDFFormat.valueOf(rdfFormat.name());
    String defaultMIMEType = format.getDefaultMIMEType();
    Model model = ModelConverter.toModel(jsonLdModel, RDFFormat.JSONLD);
    String converted = ModelConverter.modelToLang(model, format);
    return ResponseEntity.ok().contentType(MediaType.parseMediaType(defaultMIMEType)).body(converted);
  }

}
