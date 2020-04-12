package tech.artcoded.atriangle.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openrdf.model.Model;
import org.openrdf.rio.RDFFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.artcoded.atriangle.api.CheckedThreadHelper;
import tech.artcoded.atriangle.api.dto.*;
import tech.artcoded.atriangle.core.sparql.ModelConverter;

import javax.inject.Inject;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.*;
import static tech.artcoded.atriangle.api.dto.LogEventType.ERROR;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestConfig.class)
@Slf4j
@EnabledIfSystemProperty(named = "testSuite", matches = "integration")
public class ProjectTest {

  @Inject
  private TestRestTemplate restTemplate;

  @Inject
  private ObjectMapper mapper;

  @Inject
  private TestingUtils testingUtils;

  @Value("classpath:rdf-example.rdf")
  private Resource rdfExampleFile;

  @Value("classpath:excel2skos-exemple-1.xlsx")
  private Resource xlsSkosExampleFile;

  @Value("classpath:expected-output-skos-conversion.ttl")
  private Resource expectedOutputSkosConversion;

  @Value("classpath:shacl-shapes-example.ttl")
  private Resource shaclShapesExampleFile;

  @Value("classpath:shacl-valid-data-example.ttl")
  private Resource shaclValidDataExampleFile;

  @Value("classpath:shacl-bad-data-example.ttl")
  private Resource shaclBadDataExampleFile;

  @Value("classpath:sparql-ask-data.ttl")
  private Resource askDataFile;

  @Value("classpath:sparql-ask.ftl")
  private Resource askQueryFile;

  @Value("classpath:sparql-select.ftl")
  private Resource selectQueryFile;

  @Value("classpath:sparql-construct.ftl")
  private Resource constructQueryFile;
  @Value("classpath:expected-construct.ttl")
  private Resource expectedConstructFile;

  @Value("${backend.url}")
  private String backendUrl;

  @Test
  public void createProjectTest() {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    createProjectEvent(projectName);
  }

  @Test
  public void addFileToProjectTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);
    addFileToProject(projectEvent.getId(), rdfExampleFile);
  }

  @Test
  public void xls2rdfTransformationTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);
    FileEvent xlsFileEvent = addFileToProject(projectEvent.getId(), xlsSkosExampleFile);
    ProjectEvent projectEventWithSkosFileConverted = restTemplate.postForObject(backendUrl + String.format("/project/conversion/skos?projectId=%s&xlsFileEventId=%s", projectEvent.getId(), xlsFileEvent.getId()),
                                                                                testingUtils.requestWithEmptyBody(), ProjectEvent.class);
    log.info("project {}", projectEventWithSkosFileConverted);
    Optional<FileEvent> optionalSkosConvertedFile = projectEventWithSkosFileConverted.getFileEvents()
                                                                                     .stream()
                                                                                     .filter(file -> FileEventType.SKOS_PLAY_CONVERTER_OUTPUT.equals(file.getEventType()))
                                                                                     .findFirst();
    assertTrue(optionalSkosConvertedFile.isPresent());
    Model expectedModel = ModelConverter.toModel(IOUtils.toString(expectedOutputSkosConversion.getInputStream(), UTF_8), RDFFormat.TURTLE);
    FileEvent skosOutput = optionalSkosConvertedFile.get();
    ResponseEntity<ByteArrayResource> file = downloadFile(projectEventWithSkosFileConverted.getId(), skosOutput);
    assertNotNull(file.getBody());
    String modelConverted = IOUtils.toString(file.getBody().getInputStream(), UTF_8);
    assertFalse(StringUtils.isEmpty(modelConverted));
    log.info("model converted:\n{}", modelConverted);
    Model model = ModelConverter.toModel(modelConverted, RDFFormat.TURTLE);
    assertTrue(ModelConverter.equals(expectedModel, model));
    sink(projectEventWithSkosFileConverted, skosOutput, null);
    List<LogEvent> logEvents = getLogs(projectEvent);

    assertTrue(logEvents.stream()
                        .anyMatch(logEvent -> "rdf saved to triplestore"
                          .equals(logEvent.getMessage())));
    assertTrue(logEvents.stream()
                        .anyMatch(logEvent -> "rdf saved to mongodb"
                          .equals(logEvent.getMessage())));
    assertFalse(logEvents.stream()
                         .anyMatch(logEvent -> ERROR.equals(logEvent.getType())));
  }

  @Test
  public void downloadFileTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);
    FileEvent fileEvent = addFileToProject(projectEvent.getId(), rdfExampleFile);
    downloadFile(projectEvent.getId(), fileEvent);
  }

  @Test
  public void sinkProjectTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);
    // add rdf file
    FileEvent fileEvent = addFileToProject(projectEvent.getId(), rdfExampleFile);
    sink(projectEvent, fileEvent, null);

    List<LogEvent> logEvents = getLogs(projectEvent);

    assertTrue(logEvents.stream()
                        .anyMatch(logEvent -> "rdf saved to triplestore"
                          .equals(logEvent.getMessage())));
    assertTrue(logEvents.stream()
                        .anyMatch(logEvent -> "rdf saved to mongodb"
                          .equals(logEvent.getMessage())));
    assertFalse(logEvents.stream()
                         .anyMatch(logEvent -> ERROR.equals(logEvent.getType())));
  }

  @Test
  public void askSparqlQueryTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);
    // add rdf file
    FileEvent fileEvent = addFileToProject(projectEvent.getId(), askDataFile);
    sink(projectEvent, fileEvent, null);

    List<LogEvent> logEvents = getLogs(projectEvent);

    assertTrue(logEvents.stream()
                        .anyMatch(logEvent -> "rdf saved to triplestore"
                          .equals(logEvent.getMessage())));
    assertTrue(logEvents.stream()
                        .anyMatch(logEvent -> "rdf saved to mongodb"
                          .equals(logEvent.getMessage())));
    assertFalse(logEvents.stream()
                         .anyMatch(logEvent -> ERROR.equals(logEvent.getType())));

    // add ask query
    FileEvent queryFile = addSparqlQueryFileToProject(projectEvent.getId(), askQueryFile);
    Map<String, String> variables = Map.of(
      "name","Alice"
    );
    Boolean alicePresent = restTemplate.postForObject(String.format("%s/project/execute-ask-sparql-query?projectId=%s&freemarkerTemplateFileId=%s", backendUrl, projectEvent.getId(), queryFile.getId()),
                                                  testingUtils.requestWithBody(Map.of(
                                                    "name","Alice"
                                                  )), Boolean.class);
    Boolean bobPresent = restTemplate.postForObject(String.format("%s/project/execute-ask-sparql-query?projectId=%s&freemarkerTemplateFileId=%s", backendUrl, projectEvent.getId(), queryFile.getId()),
                                                  testingUtils.requestWithBody(Map.of(
                                                    "name","Bob"
                                                  )), Boolean.class);
    assertTrue(alicePresent);
    assertTrue(bobPresent);

    Boolean jenaPresent = restTemplate.postForObject(String.format("%s/project/execute-ask-sparql-query?projectId=%s&freemarkerTemplateFileId=%s", backendUrl, projectEvent.getId(), queryFile.getId()),
                                                  testingUtils.requestWithBody(Map.of(
                                                    "name","Jena"
                                                  )), Boolean.class);
    assertFalse(jenaPresent);

  }

  @Test
  public void selectSparqlQueryTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);
    // add rdf file
    FileEvent fileEvent = addFileToProject(projectEvent.getId(), rdfExampleFile);
    sink(projectEvent, fileEvent, null);

    List<LogEvent> logEvents = getLogs(projectEvent);

    assertTrue(logEvents.stream()
                        .anyMatch(logEvent -> "rdf saved to triplestore"
                          .equals(logEvent.getMessage())));
    assertTrue(logEvents.stream()
                        .anyMatch(logEvent -> "rdf saved to mongodb"
                          .equals(logEvent.getMessage())));
    assertFalse(logEvents.stream()
                         .anyMatch(logEvent -> ERROR.equals(logEvent.getType())));

    // add select query
    FileEvent queryFile = addSparqlQueryFileToProject(projectEvent.getId(), selectQueryFile);
    Map<String, String> variables = Map.of(
      "s","?s",
      "p","?p",
      "o","?o"
    );
    List<Map<String, String>> result = restTemplate.postForObject(String.format("%s/project/execute-select-sparql-query?projectId=%s&freemarkerTemplateFileId=%s", backendUrl, projectEvent.getId(), queryFile.getId()),
                                               testingUtils.requestWithBody(variables), List.class);

    log.info("result of select query:\n {}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

    assertTrue(result.stream().anyMatch(map-> "http://artcoded.tech/person".equals(map.get("s")) &&
                                          "http://artcoded.tech#artist".equals(map.get("p")) &&
                                          "Nordine Bittich".equals(map.get("o"))
    ));
    assertTrue(result.stream().anyMatch(map-> "http://artcoded.tech/person".equals(map.get("s")) &&
                                          "http://artcoded.tech#company".equals(map.get("p")) &&
                                          "Artcoded".equals(map.get("o"))
    ));
    assertTrue(result.stream().anyMatch(map-> "http://artcoded.tech/person".equals(map.get("s")) &&
                                          "http://artcoded.tech#country".equals(map.get("p")) &&
                                          "BELGIUM".equals(map.get("o"))
    ));
    assertTrue(result.stream().anyMatch(map-> "http://artcoded.tech/person".equals(map.get("s")) &&
                                          "http://artcoded.tech#year".equals(map.get("p")) &&
                                          "1988".equals(map.get("o"))
    ));

  }

  @Test
  public void constructSparqlQueryTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);
    // add rdf file
    FileEvent fileEvent = addFileToProject(projectEvent.getId(), rdfExampleFile);
    sink(projectEvent, fileEvent, null);

    List<LogEvent> logEvents = getLogs(projectEvent);

    assertTrue(logEvents.stream()
                        .anyMatch(logEvent -> "rdf saved to triplestore"
                          .equals(logEvent.getMessage())));
    assertTrue(logEvents.stream()
                        .anyMatch(logEvent -> "rdf saved to mongodb"
                          .equals(logEvent.getMessage())));
    assertFalse(logEvents.stream()
                         .anyMatch(logEvent -> ERROR.equals(logEvent.getType())));

    // add construct query
    FileEvent queryFile = addSparqlQueryFileToProject(projectEvent.getId(), constructQueryFile);
    Map<String, String> variables = Map.of(
      "toConstruct","?s ?p ?o",
      "condition", "?s ?p ?o"
    );
    String modelString = restTemplate.postForObject(String.format("%s/project/execute-construct-sparql-query?projectId=%s&freemarkerTemplateFileId=%s", backendUrl, projectEvent.getId(), queryFile.getId()),
                                               testingUtils.requestWithBody(variables), String.class);
    log.info("result of construct query:\n {}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(modelString));

    Model model = ModelConverter.toModel(modelString, RDFFormat.JSONLD);
    Model expectedModel = ModelConverter.inputStreamToModel(expectedConstructFile.getFilename(), expectedConstructFile::getInputStream);
    assertTrue(ModelConverter.equals(expectedModel, model));

  }

  @Test
  public void sinkProjectWithShaclValidationOkTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);

    // add shacl shapes file
    FileEvent shaclShapes = addFileToProject(projectEvent.getId(), shaclShapesExampleFile);
    FileEvent validData = addFileToProject(projectEvent.getId(), shaclValidDataExampleFile);
    // check shacl before sink
    ResponseEntity<String> response = restTemplate.exchange(String.format("%s/project/%s/shacl-validation?shapesFileId=%s&rdfModelFileId=%s", backendUrl,
                                                                          projectEvent.getId(), shaclShapes.getId(), validData.getId()), HttpMethod.GET,
                                                            testingUtils.requestWithEmptyBody(), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(StringUtils.isEmpty(response.getBody()));

    sink(projectEvent, validData, shaclShapes);

    List<LogEvent> logEvents = getLogs(projectEvent);

    assertTrue(logEvents.stream()
                        .anyMatch(logEvent -> "rdf saved to mongodb"
                          .equals(logEvent.getMessage())));
    assertTrue(logEvents.stream()
                        .anyMatch(logEvent -> "shacl validation ok".equals(logEvent.getMessage())));

    assertFalse(logEvents.stream()
                         .anyMatch(logEvent -> ERROR.equals(logEvent.getType())));
  }

  @Test
  public void sinkProjectWithShaclValidationNotOkTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);

    // add shacl shapes file
    FileEvent shaclShapes = addFileToProject(projectEvent.getId(), shaclShapesExampleFile);
    FileEvent badData = addFileToProject(projectEvent.getId(), shaclBadDataExampleFile);

    ResponseEntity<String> exception = restTemplate.exchange(String.format("%s/project/%s/shacl-validation?shapesFileId=%s&rdfModelFileId=%s", backendUrl,
                                                                           projectEvent.getId(), shaclShapes.getId(), badData.getId()), HttpMethod.GET,
                                                             testingUtils.requestWithEmptyBody(), String.class);
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    String expectedMessage = exception.getBody();
    assertTrue(StringUtils.isNotEmpty(expectedMessage));
    log.info("result of shacl validation: \n{}", expectedMessage);

    sink(projectEvent, badData, shaclShapes);

    List<LogEvent> logEvents = getLogs(projectEvent);
    Optional<LogEvent> shaclError = logEvents.stream()
                                             .filter(logEvent -> ERROR.equals(logEvent.getType()))
                                             .filter(logEvent -> logEvent.getMessage()
                                                                         .contains("http://www.w3.org/ns/shacl#resultMessage"))
                                             .peek(logEvent -> log.info("Shacl error: {}", logEvent.getMessage()))
                                             .findFirst();
    assertTrue(shaclError.isPresent());
    String message = shaclError.get()
                               .getMessage();
    Model expectedModel = ModelConverter.toModel(expectedMessage, RDFFormat.JSONLD);
    Model actualModel = ModelConverter.toModel(message, RDFFormat.JSONLD);
    assertTrue(ModelConverter.equals(expectedModel, actualModel));


  }

  private void sink(ProjectEvent projectEvent, FileEvent fileEvent, FileEvent shapes) {
    // sink
    log.info("sink now");
    SinkRequest sinkRequest = SinkRequest.builder()
                                         .projectId(projectEvent.getId())
                                         .rdfFileEventId(fileEvent.getId())
                                         .shaclFileEventId(ofNullable(shapes).map(FileEvent::getId)
                                                                             .orElse(null))
                                         .build();
    restTemplate.postForLocation(backendUrl + "/project/sink", testingUtils.requestWithBody(sinkRequest));
    // log output
    CheckedThreadHelper.FIVE_SECONDS.sleep();
  }

  private List<LogEvent> getLogs(ProjectEvent projectEvent) throws Exception {
    log.info("check logs");
    List<LogEvent> logEvents = Arrays.asList(requireNonNull(restTemplate.getForObject(backendUrl + "/project/" + projectEvent.getId() + "/logs", LogEvent[].class)));
    log.info("logs: \n{}", mapper.writerWithDefaultPrettyPrinter()
                                 .writeValueAsString(Arrays.asList(requireNonNull(logEvents))));
    assertFalse(logEvents.isEmpty());
    return logEvents;
  }

  private ProjectEvent createProjectEvent(String projectName) {
    log.info("project creation");
    ProjectEvent projectEvent = restTemplate.postForObject(backendUrl + "/project?name=" + projectName.toLowerCase(), testingUtils.requestWithEmptyBody(), ProjectEvent.class);
    assertNotNull(projectEvent);
    assertEquals(projectName.toLowerCase(), projectEvent.getName());
    log.info("project with id {} and name {} created", projectEvent.getId(), projectEvent.getName());
    return projectEvent;
  }

  private FileEvent addFileToProject(String projectId, Resource resource) throws Exception {
    return addFile(backendUrl + "/project/add-file", projectId, resource);
  }

  private FileEvent addSparqlQueryFileToProject(String projectId, Resource resource) throws Exception {
    return addFile(backendUrl + "/project/add-sparql-query-template", projectId, resource);
  }

  private FileEvent addFile(String url, String projectId, Resource resource) throws Exception {
    // add rdf file
    log.info("add file {} to project {}", resource.getFilename(), projectId);
    ResponseEntity<ProjectEvent> response = testingUtils.postFileToProject(projectId, url, resource.getFilename(),
                                                                           resource);
    log.info("response status {}", response.getStatusCodeValue());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ProjectEvent updatedProjectEvent = response.getBody();
    assertNotNull(updatedProjectEvent);
    assertNotNull(updatedProjectEvent
                    .getFileEvents());
    Optional<FileEvent> optionalFileEvent = updatedProjectEvent
      .getFileEvents()
      .stream()
      .filter(event -> Objects.equals(resource.getFilename(), event.getName()))
      .findFirst();
    assertTrue(optionalFileEvent.isPresent());
    FileEvent fileEvent = optionalFileEvent.get();
    assertNotNull(fileEvent.getId());
    assertEquals(resource.getFilename(), fileEvent.getOriginalFilename());
    assertEquals(resource.getFilename(), fileEvent.getName());
    log.info("file event: {}", fileEvent);
    return fileEvent;
  }

  private ResponseEntity<ByteArrayResource> downloadFile(String projectId, FileEvent fileEvent) {
    String url = String.format("%s/project/%s/download-file/%s", backendUrl, projectId, fileEvent.getId());
    log.info("url {}", url);
    ResponseEntity<ByteArrayResource> file = testingUtils.downloadFile(url);
    assertEquals(HttpStatus.OK, file.getStatusCode());
    assertNotNull(file.getBody());
    assertEquals(fileEvent.getContentType(), file.getHeaders()
                                                 .getContentType()
                                                 .toString());
    assertEquals(fileEvent.getOriginalFilename(), file.getHeaders()
                                                      .getContentDisposition()
                                                      .getFilename());
    return file;
  }
}
