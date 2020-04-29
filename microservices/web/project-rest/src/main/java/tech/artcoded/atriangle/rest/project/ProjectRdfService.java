package tech.artcoded.atriangle.rest.project;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.DateHelper;
import tech.artcoded.atriangle.api.dto.FileEvent;
import tech.artcoded.atriangle.api.dto.FileEventType;
import tech.artcoded.atriangle.api.dto.LogEvent;
import tech.artcoded.atriangle.api.dto.ProjectEvent;
import tech.artcoded.atriangle.core.kafka.LoggerAction;
import tech.artcoded.atriangle.feign.clients.elastic.ElasticRestFeignClient;
import tech.artcoded.atriangle.feign.clients.shacl.ShaclRestFeignClient;
import tech.artcoded.atriangle.feign.clients.sparql.SparqlRestFeignClient;
import tech.artcoded.atriangle.feign.clients.util.FeignMultipartFile;
import tech.artcoded.atriangle.feign.clients.xls2rdf.Xls2RdfRestFeignClient;

import javax.inject.Inject;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class ProjectRdfService {

  private static final String DERIVED_FILE_REGEX = "-derived-output-";
  private static final String DERIVED_FILE_SKOS_REGEX = DERIVED_FILE_REGEX + "skos-";

  private final ElasticRestFeignClient elasticRestFeignClient;
  private final ShaclRestFeignClient shaclRestFeignClient;
  private final Xls2RdfRestFeignClient skosPlayRestFeignClient;
  private final SparqlRestFeignClient sparqlRestFeignClient;
  private final ProjectService projectService;
  private final ProjectFileService projectFileService;

  @Inject
  public ProjectRdfService(
      ElasticRestFeignClient elasticRestFeignClient,
      ShaclRestFeignClient shaclRestFeignClient,
      Xls2RdfRestFeignClient skosPlayRestFeignClient,
      SparqlRestFeignClient sparqlRestFeignClient,
      ProjectService projectService,
      ProjectFileService projectFileService,
      LoggerAction loggerAction) {
    this.elasticRestFeignClient = elasticRestFeignClient;
    this.shaclRestFeignClient = shaclRestFeignClient;
    this.skosPlayRestFeignClient = skosPlayRestFeignClient;
    this.sparqlRestFeignClient = sparqlRestFeignClient;
    this.projectService = projectService;
    this.projectFileService = projectFileService;
  }

  public Optional<List<LogEvent>> getLogsForProject(String projectId) {
    return projectService
        .findById(projectId)
        .map(projectEvent -> elasticRestFeignClient.getLogsByCorrelationId(projectId));
  }

  public ResponseEntity<String> shaclValidation(
      String projectId, String shapesFileId, String rdfModelFileId) {
    return shaclRestFeignClient.validate(projectId, shapesFileId, rdfModelFileId);
  }

  @SneakyThrows
  public Optional<ProjectEvent> skosConversion(
      String projectId,
      boolean labelSkosXl,
      boolean ignorePostTreatmentsSkos,
      FileEvent xlsFileEvent) {

    MultipartFile xlsInput =
        FeignMultipartFile.builder()
            .contentType(xlsFileEvent.getContentType())
            .name(xlsFileEvent.getName())
            .originalFilename(xlsFileEvent.getName())
            .bytes(
                projectFileService
                    .downloadFile(projectId, xlsFileEvent.getId())
                    .getBody()
                    .getByteArray())
            .build();
    String contentType = "text/turtle";
    ResponseEntity<ByteArrayResource> response =
        skosPlayRestFeignClient.convertRDF(
            "file",
            xlsInput,
            "fr",
            null,
            contentType,
            labelSkosXl,
            false,
            false,
            ignorePostTreatmentsSkos);

    ByteArrayResource body = response.getBody();

    String baseFileName = FilenameUtils.removeExtension(xlsFileEvent.getName());
    String outputFilename =
        baseFileName.split(DERIVED_FILE_REGEX)[0]
            + DERIVED_FILE_SKOS_REGEX
            + DateHelper.formatCurrentDateForFilename()
            + ".ttl";
    MultipartFile rdfOutput =
        FeignMultipartFile.builder()
            .contentType(contentType)
            .name(outputFilename)
            .originalFilename(outputFilename)
            .bytes(body.getByteArray())
            .build();

    return this.projectFileService
        .addFile(projectId, rdfOutput, FileEventType.SKOS_PLAY_CONVERTER_OUTPUT)
        .map(Map.Entry::getValue);
  }

  @SneakyThrows
  @Cacheable(cacheNames = "selectSparqlQuery", key = "#cacheKey")
  public List<Map<String, String>> executeSelectSparqlQuery(
      ProjectEvent project, String compiledQuery, String cacheKey) {

    ResponseEntity<List<Map<String, String>>> response =
        sparqlRestFeignClient.selectQuery(compiledQuery, project.getName());

    return response.getBody();
  }

  @SneakyThrows
  @Cacheable(cacheNames = "constructSparqlQuery", key = "#cacheKey")
  public String executeConstructSparqlQuery(
      ProjectEvent project, String compiledQuery, String cacheKey) {

    ResponseEntity<String> response =
        sparqlRestFeignClient.constructQuery(compiledQuery, project.getName());

    return response.getBody();
  }

  @SneakyThrows
  @Cacheable(cacheNames = "askSparqlQuery", key = "#cacheKey")
  public Boolean executeAskSparqlQuery(
      ProjectEvent project, String compiledQuery, String cacheKey) {

    ResponseEntity<Boolean> response =
        sparqlRestFeignClient.askQuery(compiledQuery, project.getName());

    return response.getBody();
  }

  @SneakyThrows
  public String compileQuery(String templateQuery, Map<String, String> variables) {

    Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
    Template selectSparqlQuery = new Template("t", new StringReader(templateQuery), cfg);

    return FreeMarkerTemplateUtils.processTemplateIntoString(selectSparqlQuery, variables);
  }

  @SneakyThrows
  @Cacheable(cacheNames = "queryTemplate", key = "{#project.id, #freemarkerTemplateFileId}")
  public String getCachedQueryTemplate(ProjectEvent project, String freemarkerTemplateFileId) {

    ResponseEntity<ByteArrayResource> freemarkerTemplate =
        projectFileService.downloadFile(project.getId(), freemarkerTemplateFileId);
    return IOUtils.toString(
        Objects.requireNonNull(freemarkerTemplate.getBody()).getInputStream(),
        StandardCharsets.UTF_8);
  }
}
