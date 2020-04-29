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
import tech.artcoded.atriangle.api.dto.SparqlQueryRequest.SparqlQueryRequestType;
import tech.artcoded.atriangle.core.sparql.ModelConverter;

import javax.inject.Inject;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.*;
import static tech.artcoded.atriangle.api.dto.FileEventType.*;
import static tech.artcoded.atriangle.api.dto.LogEventType.ERROR;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestConfig.class)
@Slf4j
@EnabledIfSystemProperty(named = "testSuite", matches = "integration")
public class ProjectTest {

  @Inject private TestRestTemplate restTemplate;

  @Inject private ObjectMapper mapper;

  @Inject private TestingUtils testingUtils;

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

  @Value("classpath:sparql-full-text-search.ftl")
  private Resource fullTextSearchFile;

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
  public void createProjectWithDescriptionTest() {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    String description = RandomStringUtils.randomAlphanumeric(255);
    log.info("project creation");
    ProjectEvent projectEvent =
        restTemplate.postForObject(
            backendUrl + "/project?name=" + projectName + "&description=" + description,
            testingUtils.requestWithEmptyBody(),
            ProjectEvent.class);
    assertNotNull(projectEvent);
    assertEquals(projectName.toLowerCase(), projectEvent.getName());
    assertEquals(description, projectEvent.getDescription());
    assertNotNull(projectEvent.getCreationDate());
    log.info(
        "project with id {} and name {} created", projectEvent.getId(), projectEvent.getName());
  }

  @Test
  public void updateProjectDescriptionTest() {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    String description = RandomStringUtils.randomAlphanumeric(255);
    ProjectEvent projectEvent = createProjectEvent(projectName);
    ResponseEntity<ProjectEvent> projectWithUpdatedDescription =
        restTemplate.exchange(
            backendUrl
                + "/project/"
                + projectEvent.getId()
                + "/update-description?description="
                + description,
            HttpMethod.POST,
            testingUtils.requestWithEmptyBody(),
            ProjectEvent.class);
    assertEquals(HttpStatus.OK, projectWithUpdatedDescription.getStatusCode());
    assertNotNull(projectWithUpdatedDescription.getBody());
    assertNotNull(projectWithUpdatedDescription.getBody().getLastModifiedDate());

    assertEquals(description, projectWithUpdatedDescription.getBody().getDescription());
  }

  @Test
  public void createProjectAlreadyExistTest() {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    createProjectEvent(projectName);

    ResponseEntity<String> responseAlreadyExist =
        restTemplate.exchange(
            backendUrl + "/project?name=" + projectName,
            HttpMethod.POST,
            testingUtils.requestWithEmptyBody(),
            String.class);
    assertEquals(HttpStatus.BAD_REQUEST, responseAlreadyExist.getStatusCode());
    assertEquals(
        String.format(
            "cannot create project %s. name not valid (minimum 7 alphabetic characters) or already exist",
            projectName),
        responseAlreadyExist.getBody());

    String projectNameWithNumbers = RandomStringUtils.randomAlphanumeric(6).concat("9");

    ResponseEntity<String> responseNameNotValid =
        restTemplate.exchange(
            backendUrl + "/project?name=" + projectNameWithNumbers,
            HttpMethod.POST,
            testingUtils.requestWithEmptyBody(),
            String.class);

    assertEquals(HttpStatus.BAD_REQUEST, responseNameNotValid.getStatusCode());
    assertEquals(
        String.format(
            "cannot create project %s. name not valid (minimum 7 alphabetic characters) or already exist",
            projectNameWithNumbers),
        responseNameNotValid.getBody());

    String shortProjectName = RandomStringUtils.randomAlphabetic(6);
    ResponseEntity<String> responseNameTooShort =
        restTemplate.exchange(
            backendUrl + "/project?name=" + shortProjectName,
            HttpMethod.POST,
            testingUtils.requestWithEmptyBody(),
            String.class);

    assertEquals(HttpStatus.BAD_REQUEST, responseNameTooShort.getStatusCode());
    assertEquals(
        String.format(
            "cannot create project %s. name not valid (minimum 7 alphabetic characters) or already exist",
            shortProjectName),
        responseNameTooShort.getBody());
  }

  @Test
  public void addFileToProjectTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);
    addFileToProject(projectEvent.getId(), RDF_FILE, rdfExampleFile);
  }

  @Test
  public void addBadFileToProjectTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);

    ResponseEntity<String> response =
        testingUtils.postFileToProject(
            Map.of("projectId", projectEvent.getId()),
            backendUrl + "/project/add-rdf-file",
            xlsSkosExampleFile.getFilename(),
            xlsSkosExampleFile,
            String.class);
    assertSame(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    assertEquals("the file is not an rdf file", response.getBody());
  }

  @Test
  public void xls2rdfTransformationTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);
    FileEvent xlsFileEvent = addFileToProject(projectEvent.getId(), SKOS_FILE, xlsSkosExampleFile);
    ProjectEvent projectEventWithSkosFileConverted =
        restTemplate.postForObject(
            backendUrl
                + String.format(
                    "/project/conversion/skos?projectId=%s&xlsFileEventId=%s",
                    projectEvent.getId(), xlsFileEvent.getId()),
            testingUtils.requestWithEmptyBody(),
            ProjectEvent.class);
    log.info("project {}", projectEventWithSkosFileConverted);
    Optional<FileEvent> optionalSkosConvertedFile =
        projectEventWithSkosFileConverted.getFileEvents().stream()
            .filter(file -> FileEventType.SKOS_PLAY_CONVERTER_OUTPUT.equals(file.getEventType()))
            .findFirst();
    assertTrue(optionalSkosConvertedFile.isPresent());
    Model expectedModel =
        ModelConverter.toModel(
            IOUtils.toString(expectedOutputSkosConversion.getInputStream(), UTF_8),
            RDFFormat.TURTLE);
    FileEvent skosOutput = optionalSkosConvertedFile.get();
    ResponseEntity<ByteArrayResource> file =
        downloadFile(projectEventWithSkosFileConverted.getId(), skosOutput);
    assertNotNull(file.getBody());
    String modelConverted = IOUtils.toString(file.getBody().getInputStream(), UTF_8);
    assertFalse(StringUtils.isEmpty(modelConverted));
    log.info("model converted:\n{}", modelConverted);
    Model model = ModelConverter.toModel(modelConverted, RDFFormat.TURTLE);
    assertTrue(ModelConverter.equals(expectedModel, model));
    sink(projectEventWithSkosFileConverted, skosOutput, null);
    checkLogs(projectEvent);
  }

  @Test
  public void badSkosConversionTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);
    FileEvent badSkosFile = addFileToProject(projectEvent.getId(), RDF_FILE, rdfExampleFile);
    ;
    ResponseEntity<String> response =
        restTemplate.exchange(
            backendUrl
                + String.format(
                    "/project/conversion/skos?projectId=%s&xlsFileEventId=%s",
                    projectEvent.getId(), badSkosFile.getId()),
            HttpMethod.POST,
            testingUtils.requestWithEmptyBody(),
            String.class);
    assertSame(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    assertEquals("only xlsx type supported", response.getBody());
  }

  @Test
  public void downloadFileTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);
    FileEvent fileEvent = addFileToProject(projectEvent.getId(), RDF_FILE, rdfExampleFile);
    downloadFile(projectEvent.getId(), fileEvent);
  }

  @Test
  public void sinkProjectTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);
    // add rdf file
    FileEvent fileEvent = addFileToProject(projectEvent.getId(), RDF_FILE, rdfExampleFile);
    sink(projectEvent, fileEvent, null);

    checkLogs(projectEvent);
  }

  @Test
  public void askSparqlQueryTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);
    // add rdf file
    FileEvent fileEvent = addFileToProject(projectEvent.getId(), RDF_FILE, askDataFile);
    sink(projectEvent, fileEvent, null);

    checkLogs(projectEvent);

    // add ask query
    FileEvent queryFile =
        addFile(
            backendUrl + "/project/add-sparql-query-template",
            askQueryFile,
            Map.of(
                "projectId", projectEvent.getId(),
                "queryType", SparqlQueryRequestType.ASK_QUERY.name()));

    SparqlQueryRequest.SparqlQueryRequestBuilder askBuilder =
        SparqlQueryRequest.builder()
            .projectId(projectEvent.getId())
            .freemarkerTemplateFileId(queryFile.getId())
            .type(SparqlQueryRequestType.ASK_QUERY);
    SparqlQueryRequest alicePresentRequest = askBuilder.variables(Map.of("name", "Alice")).build();
    SparqlQueryRequest bobPresentRequest = askBuilder.variables(Map.of("name", "Bob")).build();
    SparqlQueryRequest jenaNotPresentRequest = askBuilder.variables(Map.of("name", "Jena")).build();

    SparqlQueryResponse alicePresent =
        restTemplate.postForObject(
            String.format("%s/project/execute-sparql-query", backendUrl),
            testingUtils.requestWithBody(alicePresentRequest),
            SparqlQueryResponse.class);
    assertNotNull(alicePresent.getResponse());
    assertTrue(Boolean.parseBoolean(alicePresent.getResponse().toString()));

    SparqlQueryResponse bobPresent =
        restTemplate.postForObject(
            String.format("%s/project/execute-sparql-query", backendUrl),
            testingUtils.requestWithBody(bobPresentRequest),
            SparqlQueryResponse.class);

    assertNotNull(bobPresent.getResponse());
    assertTrue(Boolean.parseBoolean(bobPresent.getResponse().toString()));

    SparqlQueryResponse jenaNotPresent =
        restTemplate.postForObject(
            String.format("%s/project/execute-sparql-query", backendUrl),
            testingUtils.requestWithBody(jenaNotPresentRequest),
            SparqlQueryResponse.class);
    assertNotNull(jenaNotPresent.getResponse());
    assertFalse(Boolean.parseBoolean(jenaNotPresent.getResponse().toString()));
  }

  @Test
  public void selectSparqlQueryTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);
    // add rdf file
    FileEvent fileEvent = addFileToProject(projectEvent.getId(), RDF_FILE, rdfExampleFile);
    sink(projectEvent, fileEvent, null);

    checkLogs(projectEvent);

    // add select query
    FileEvent queryFile =
        addFile(
            backendUrl + "/project/add-sparql-query-template",
            selectQueryFile,
            Map.of(
                "projectId", projectEvent.getId(),
                "queryType", SparqlQueryRequestType.SELECT_QUERY.name()));

    SparqlQueryRequest selectQuery =
        SparqlQueryRequest.builder()
            .projectId(projectEvent.getId())
            .freemarkerTemplateFileId(queryFile.getId())
            .variables(
                Map.of(
                    "s", "?s",
                    "p", "?p",
                    "o", "?o"))
            .type(SparqlQueryRequestType.SELECT_QUERY)
            .build();
    SparqlQueryResponse result =
        restTemplate.postForObject(
            String.format("%s/project/execute-sparql-query", backendUrl),
            testingUtils.requestWithBody(selectQuery),
            SparqlQueryResponse.class);

    log.info(
        "result of select query:\n {}",
        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

    assertNotNull(result);
    List<Map<String, String>> responseSelect = (List<Map<String, String>>) result.getResponse();

    assertTrue(
        responseSelect.stream()
            .anyMatch(
                map ->
                    "http://artcoded.tech/person".equals(map.get("s"))
                        && "http://artcoded.tech#artist".equals(map.get("p"))
                        && "Nordine Bittich".equals(map.get("o"))));
    assertTrue(
        responseSelect.stream()
            .anyMatch(
                map ->
                    "http://artcoded.tech/person".equals(map.get("s"))
                        && "http://artcoded.tech#company".equals(map.get("p"))
                        && "Artcoded".equals(map.get("o"))));
    assertTrue(
        responseSelect.stream()
            .anyMatch(
                map ->
                    "http://artcoded.tech/person".equals(map.get("s"))
                        && "http://artcoded.tech#country".equals(map.get("p"))
                        && "BELGIUM".equals(map.get("o"))));
    assertTrue(
        responseSelect.stream()
            .anyMatch(
                map ->
                    "http://artcoded.tech/person".equals(map.get("s"))
                        && "http://artcoded.tech#year".equals(map.get("p"))
                        && "1988".equals(map.get("o"))));
  }

  @Test
  public void fullTextSearchSparqlQueryTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);
    // add rdf file
    FileEvent fileEvent = addFileToProject(projectEvent.getId(), RDF_FILE, rdfExampleFile);
    sink(projectEvent, fileEvent, null);

    // add full text search query

    FileEvent queryFile =
        addFile(
            backendUrl + "/project/add-sparql-query-template",
            fullTextSearchFile,
            Map.of(
                "projectId", projectEvent.getId(),
                "queryType", SparqlQueryRequestType.SELECT_QUERY.name()));

    SparqlQueryRequest selectQuery =
        SparqlQueryRequest.builder()
            .projectId(projectEvent.getId())
            .freemarkerTemplateFileId(queryFile.getId())
            .variables(
                Map.of(
                    "searchTerm", "Nordine",
                    "minRelevance", "0.25",
                    "matchAllTerm", "true",
                    "maxRank", "1000"))
            .type(SparqlQueryRequestType.SELECT_QUERY)
            .build();
    SparqlQueryResponse result =
        restTemplate.postForObject(
            String.format("%s/project/execute-sparql-query", backendUrl),
            testingUtils.requestWithBody(selectQuery),
            SparqlQueryResponse.class);

    log.info(
        "result of select query:\n {}",
        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
    assertNotNull(result);
    List<Map<String, String>> responseSelect = (List<Map<String, String>>) result.getResponse();

    assertTrue(
        responseSelect.stream()
            .anyMatch(
                map ->
                    "http://artcoded.tech/person".equals(map.get("s"))
                        && "http://artcoded.tech#artist".equals(map.get("p"))
                        && "1".equals(map.get("rank"))
                        && "0.625".equals(map.get("score"))
                        && "Nordine Bittich".equals(map.get("o"))));
  }

  @Test
  public void constructSparqlQueryTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);
    // add rdf file
    FileEvent fileEvent = addFileToProject(projectEvent.getId(), RDF_FILE, rdfExampleFile);
    sink(projectEvent, fileEvent, null);

    checkLogs(projectEvent);

    // add construct query
    FileEvent queryFile =
        addFile(
            backendUrl + "/project/add-sparql-query-template",
            constructQueryFile,
            Map.of(
                "projectId", projectEvent.getId(),
                "queryType", SparqlQueryRequestType.CONSTRUCT_QUERY.name()));

    SparqlQueryRequest constructQuery =
        SparqlQueryRequest.builder()
            .projectId(projectEvent.getId())
            .freemarkerTemplateFileId(queryFile.getId())
            .variables(
                Map.of(
                    "toConstruct", "?s ?p ?o",
                    "condition", "?s ?p ?o"))
            .type(SparqlQueryRequestType.CONSTRUCT_QUERY)
            .build();
    SparqlQueryResponse result =
        restTemplate.postForObject(
            String.format("%s/project/execute-sparql-query", backendUrl),
            testingUtils.requestWithBody(constructQuery),
            SparqlQueryResponse.class);
    assertNotNull(result.getResponse());
    log.info(
        "result of construct query:\n {}",
        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

    Model model = ModelConverter.toModel(result.getResponse().toString(), RDFFormat.JSONLD);
    Model expectedModel =
        ModelConverter.inputStreamToModel(
            expectedConstructFile.getFilename(), expectedConstructFile::getInputStream);
    assertTrue(ModelConverter.equals(expectedModel, model));
  }

  @Test
  public void sinkProjectWithShaclValidationOkTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);

    // add shacl shapes file
    FileEvent shaclShapes =
        addFileToProject(projectEvent.getId(), SHACL_FILE, shaclShapesExampleFile);
    FileEvent validData =
        addFileToProject(projectEvent.getId(), RDF_FILE, shaclValidDataExampleFile);
    // check shacl before sink
    ResponseEntity<String> response =
        restTemplate.exchange(
            String.format(
                    "%s/project/%s/shacl-validation?shapesFileId=%s&rdfModelFileId=%s",
                    backendUrl, projectEvent.getId(), shaclShapes.getId(), validData.getId()),
                HttpMethod.GET,
            testingUtils.requestWithEmptyBody(), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(StringUtils.isEmpty(response.getBody()));

    sink(projectEvent, validData, shaclShapes);

    List<LogEvent> logEvents = getLogs(projectEvent);

    assertTrue(
        logEvents.stream()
            .anyMatch(logEvent -> "rdf saved to mongodb".equals(logEvent.getMessage())));
    assertTrue(
        logEvents.stream()
            .anyMatch(logEvent -> "shacl validation ok".equals(logEvent.getMessage())));

    assertFalse(logEvents.stream().anyMatch(logEvent -> ERROR.equals(logEvent.getType())));
  }

  @Test
  public void sinkProjectWithShaclValidationNotOkTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);

    // add shacl shapes file
    FileEvent shaclShapes =
        addFileToProject(projectEvent.getId(), SHACL_FILE, shaclShapesExampleFile);
    FileEvent badData = addFileToProject(projectEvent.getId(), RDF_FILE, shaclBadDataExampleFile);

    ResponseEntity<String> exception =
        restTemplate.exchange(
            String.format(
                    "%s/project/%s/shacl-validation?shapesFileId=%s&rdfModelFileId=%s",
                    backendUrl, projectEvent.getId(), shaclShapes.getId(), badData.getId()),
                HttpMethod.GET,
            testingUtils.requestWithEmptyBody(), String.class);
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    String expectedMessage = exception.getBody();
    assertTrue(StringUtils.isNotEmpty(expectedMessage));
    log.info("result of shacl validation: \n{}", expectedMessage);

    sink(projectEvent, badData, shaclShapes);

    List<LogEvent> logEvents = getLogs(projectEvent);
    Optional<LogEvent> shaclError =
        logEvents.stream()
            .filter(logEvent -> ERROR.equals(logEvent.getType()))
            .filter(
                logEvent ->
                    logEvent.getMessage().contains("http://www.w3.org/ns/shacl#resultMessage"))
            .peek(logEvent -> log.info("Shacl error: {}", logEvent.getMessage()))
            .findFirst();
    assertTrue(shaclError.isPresent());
    String message = shaclError.get().getMessage();
    Model expectedModel = ModelConverter.toModel(expectedMessage, RDFFormat.JSONLD);
    Model actualModel = ModelConverter.toModel(message, RDFFormat.JSONLD);
    assertTrue(ModelConverter.equals(expectedModel, actualModel));
  }

  private void sink(ProjectEvent projectEvent, FileEvent fileEvent, FileEvent shapes) {
    // sink
    log.info("sink now");
    SinkRequest sinkRequest =
        SinkRequest.builder()
            .projectId(projectEvent.getId())
            .rdfFileEventId(fileEvent.getId())
            .shaclFileEventId(ofNullable(shapes).map(FileEvent::getId).orElse(null))
            .build();
    restTemplate.postForLocation(
        backendUrl + "/project/sink", testingUtils.requestWithBody(sinkRequest));
    // log output
    CheckedThreadHelper.FIVE_SECONDS.sleep();
  }

  private List<LogEvent> getLogs(ProjectEvent projectEvent) throws Exception {
    log.info("check logs");
    List<LogEvent> logEvents =
        Arrays.asList(
            requireNonNull(
                restTemplate.getForObject(
                    backendUrl + "/project/" + projectEvent.getId() + "/logs", LogEvent[].class)));
    log.info(
        "logs: \n{}",
        mapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(Arrays.asList(requireNonNull(logEvents))));
    assertFalse(logEvents.isEmpty());
    return logEvents;
  }

  private ProjectEvent createProjectEvent(String projectName) {
    log.info("project creation");
    ProjectEvent projectEvent =
        restTemplate.postForObject(
            backendUrl + "/project?name=" + projectName,
            testingUtils.requestWithEmptyBody(),
            ProjectEvent.class);
    assertNotNull(projectEvent);
    assertEquals(projectName.toLowerCase(), projectEvent.getName());
    assertEquals("N/A", projectEvent.getDescription());
    assertNotNull(projectEvent.getCreationDate());
    log.info(
        "project with id {} and name {} created", projectEvent.getId(), projectEvent.getName());
    return projectEvent;
  }

  private FileEvent addFileToProject(
      String projectId, FileEventType fileEventType, Resource resource) throws Exception {
    Map<String, String> params = Map.of("projectId", projectId);
    switch (fileEventType) {
      case RDF_FILE:
        return addFile(backendUrl + "/project/add-rdf-file", resource, params);
      case SHACL_FILE:
        return addFile(backendUrl + "/project/add-shacl-file", resource, params);
      case SKOS_FILE:
        return addFile(backendUrl + "/project/add-skos-file", resource, params);
      case PROJECT_FILE:
      case RAW_FILE:
        return addFile(backendUrl + "/project/add-raw-file", resource, params);
      default:
        throw new RuntimeException("file event type not supported yet");
    }
  }

  private FileEvent addFile(String url, Resource resource, Map<String, String> params)
      throws Exception {
    // add rdf file
    ResponseEntity<ProjectEvent> response =
        testingUtils.postFileToProject(
            params, url, resource.getFilename(), resource, ProjectEvent.class);
    log.info("response status {}", response.getStatusCodeValue());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ProjectEvent updatedProjectEvent = response.getBody();
    assertNotNull(updatedProjectEvent);
    assertNotNull(updatedProjectEvent.getFileEvents());
    Optional<FileEvent> optionalFileEvent =
        updatedProjectEvent.getFileEvents().stream()
            .filter(event -> Objects.equals(resource.getFilename(), event.getName()))
            .findFirst();
    assertTrue(optionalFileEvent.isPresent());
    FileEvent fileEvent = optionalFileEvent.get();
    assertNotNull(fileEvent.getId());
    assertEquals(resource.getFilename(), fileEvent.getOriginalFilename());
    assertEquals(resource.getFilename(), fileEvent.getName());
    assertNotNull(updatedProjectEvent.getLastModifiedDate());
    log.info("file event: {}", fileEvent);
    return fileEvent;
  }

  private void checkLogs(ProjectEvent projectEvent) throws Exception {
    List<LogEvent> logEvents = getLogs(projectEvent);

    assertTrue(
        logEvents.stream()
            .anyMatch(logEvent -> "rdf saved to triplestore".equals(logEvent.getMessage())));
    assertTrue(
        logEvents.stream()
            .anyMatch(logEvent -> "rdf saved to mongodb".equals(logEvent.getMessage())));
    assertFalse(logEvents.stream().anyMatch(logEvent -> ERROR.equals(logEvent.getType())));
  }

  private ResponseEntity<ByteArrayResource> downloadFile(String projectId, FileEvent fileEvent) {
    String url =
        String.format("%s/project/%s/download-file/%s", backendUrl, projectId, fileEvent.getId());
    log.info("url {}", url);
    ResponseEntity<ByteArrayResource> file = testingUtils.downloadFile(url);
    assertEquals(HttpStatus.OK, file.getStatusCode());
    assertNotNull(file.getBody());
    assertEquals(fileEvent.getContentType(), file.getHeaders().getContentType().toString());
    assertEquals(
        fileEvent.getOriginalFilename(), file.getHeaders().getContentDisposition().getFilename());
    return file;
  }
}
