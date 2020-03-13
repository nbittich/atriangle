package tech.artcoded.atriangle.rest.shacl;

import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openrdf.rio.RDFFormat;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tech.artcoded.atriangle.api.CheckedSupplier;
import tech.artcoded.atriangle.api.kafka.FileEvent;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.core.sparql.ModelConverter;
import tech.artcoded.atriangle.core.sparql.ShaclValidator;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.io.FilenameUtils.getExtension;


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
               consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
               produces = "text/turtle")
  @SneakyThrows
  public ResponseEntity<String> validate(@RequestParam("shaclFileEvent") FileEvent shaclFileEvent,
                                         @RequestParam("modelFileEvent") FileEvent modelFileEvent) {

    ResponseEntity<ByteArrayResource> shaclDownload = fileRestFeignClient.download(shaclFileEvent.getId());
    ResponseEntity<ByteArrayResource> modelDownload = fileRestFeignClient.download(modelFileEvent.getId());

    CheckedSupplier<InputStream> shaclFile = () -> requireNonNull(shaclDownload.getBody()).getInputStream();
    CheckedSupplier<InputStream> modelFile = () -> requireNonNull(modelDownload.getBody()).getInputStream();

    String shaclTurtle = ModelConverter.inputStreamToLang(requireNonNull(getExtension(shaclFileEvent.getOriginalFilename())), shaclFile, RDFFormat.TURTLE);
    String rdfTurtle = ModelConverter.inputStreamToLang(requireNonNull(getExtension(modelFileEvent.getOriginalFilename())), modelFile, RDFFormat.TURTLE);

    Optional<String> report = ShaclValidator.validate(rdfTurtle, shaclTurtle);

    return report.map(ResponseEntity.badRequest()::body).orElseGet(ResponseEntity.ok()::build);
  }
}
