package tech.artcoded.atriangle.rest.shacl;

import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.artcoded.atriangle.api.CheckedSupplier;
import tech.artcoded.atriangle.api.dto.FileEvent;
import tech.artcoded.atriangle.core.rest.annotation.SwaggerHeaderAuthentication;
import tech.artcoded.atriangle.core.rest.controller.BuildInfoControllerTrait;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;
import tech.artcoded.atriangle.feign.clients.shacl.ShaclRestFeignClient;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@RestController
@ApiOperation("Shacl Validation Rest")
@Slf4j
public class ShaclRestController
    implements PingControllerTrait, BuildInfoControllerTrait, ShaclRestFeignClient {
  private final FileRestFeignClient fileRestFeignClient;
  @Getter private final BuildProperties buildProperties;

  @Inject
  public ShaclRestController(
      FileRestFeignClient fileRestFeignClient, BuildProperties buildProperties) {
    this.fileRestFeignClient = fileRestFeignClient;
    this.buildProperties = buildProperties;
  }

  @SneakyThrows
  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<String> validate(
      String correlationId, String shaclFileEventId, String modelFileEventId) {

    FileEvent shaclFileEvent = fileRestFeignClient.findById(shaclFileEventId).getBody();
    FileEvent modelFileEvent = fileRestFeignClient.findById(modelFileEventId).getBody();

    ResponseEntity<ByteArrayResource> shaclDownload =
        fileRestFeignClient.download(shaclFileEventId, correlationId);
    ResponseEntity<ByteArrayResource> modelDownload =
        fileRestFeignClient.download(modelFileEventId, correlationId);

    CheckedSupplier<String> shaclFile =
        () ->
            IOUtils.toString(
                requireNonNull(shaclDownload.getBody()).getInputStream(), StandardCharsets.UTF_8);
    CheckedSupplier<String> modelFile =
        () ->
            IOUtils.toString(
                requireNonNull(modelDownload.getBody()).getInputStream(), StandardCharsets.UTF_8);

    Optional<String> report =
        ShaclValidationUtils.validate(
            modelFile.safeGet(),
            RDFLanguages.filenameToLang(modelFileEvent.getOriginalFilename()),
            shaclFile.safeGet(),
            RDFLanguages.filenameToLang(shaclFileEvent.getOriginalFilename()));

    return report.map(ResponseEntity.badRequest()::body).orElseGet(ResponseEntity.ok()::build);
  }

  @Override
  @SwaggerHeaderAuthentication
  public ResponseEntity<String> test(String shaclRules, String sampleData) {

    Optional<String> report =
        ShaclValidationUtils.validate(sampleData, Lang.TURTLE, shaclRules, Lang.TURTLE);
    return report.map(ResponseEntity.badRequest()::body).orElseGet(ResponseEntity.ok()::build);
  }
}
