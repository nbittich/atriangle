package tech.artcoded.atriangle.rest.shacl;

import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tech.artcoded.atriangle.api.CheckedSupplier;
import tech.artcoded.atriangle.api.kafka.FileEvent;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.util.Objects.requireNonNull;


@CrossOriginRestController
@ApiOperation("Shacl Validation Rest")
@Slf4j
public class ShaclRestController implements PingControllerTrait {
  private final FileRestFeignClient fileRestFeignClient;

  @Inject
  public ShaclRestController(FileRestFeignClient fileRestFeignClient) {
    this.fileRestFeignClient = fileRestFeignClient;
  }


  @PostMapping(path = "/validate",
               consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @SneakyThrows
  public ResponseEntity<String> validate(@RequestParam("shaclFileEvent") FileEvent shaclFileEvent,
                                         @RequestParam("modelFileEvent") FileEvent modelFileEvent) {

    ResponseEntity<ByteArrayResource> shaclDownload = fileRestFeignClient.download(shaclFileEvent.getId());
    ResponseEntity<ByteArrayResource> modelDownload = fileRestFeignClient.download(modelFileEvent.getId());

    CheckedSupplier<String> shaclFile = () -> IOUtils.toString(requireNonNull(shaclDownload.getBody()).getInputStream(), StandardCharsets.UTF_8);
    CheckedSupplier<String> modelFile = () -> IOUtils.toString(requireNonNull(modelDownload.getBody()).getInputStream(), StandardCharsets.UTF_8);


    Optional<String> report = ShaclValidationUtils.validate(modelFile.safeGet(),
                                                            RDFLanguages.filenameToLang(modelFileEvent.getOriginalFilename()),
                                                            shaclFile.safeGet(),
                                                            RDFLanguages.filenameToLang(shaclFileEvent.getOriginalFilename())
    );

    return report.map(ResponseEntity.badRequest()::body).orElseGet(ResponseEntity.ok()::build);
  }

  @PostMapping(path = "/test",
               consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<String> test(@RequestParam("shaclTurtleRules") String shaclRules,
                                     @RequestParam("sampleTurtleData") String sampleData) {

    Optional<String> report = ShaclValidationUtils.validate(sampleData, Lang.TURTLE, shaclRules, Lang.TURTLE);
    return report.map(ResponseEntity.badRequest()::body).orElseGet(ResponseEntity.ok()::build);
  }
}