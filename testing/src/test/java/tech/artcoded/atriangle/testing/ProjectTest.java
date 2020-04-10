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
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import tech.artcoded.atriangle.api.CheckedThreadHelper;
import tech.artcoded.atriangle.api.dto.FileEvent;
import tech.artcoded.atriangle.api.dto.LogEvent;
import tech.artcoded.atriangle.api.dto.ProjectEvent;
import tech.artcoded.atriangle.api.dto.SinkRequest;
import tech.artcoded.atriangle.core.sparql.ModelConverter;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
  private RestTemplate restTemplate;

  @Inject
  private ObjectMapper mapper;

  @Inject
  private TestingUtils testingUtils;

  @Value("classpath:rdf-example.rdf")
  private Resource rdfExampleFile;

  @Value("classpath:shacl-shapes-example.ttl")
  private Resource shaclShapesExampleFile;

  @Value("classpath:shacl-valid-data-example.ttl")
  private Resource shaclValidDataExampleFile;

  @Value("classpath:shacl-bad-data-example.ttl")
  private Resource shaclBadDataExampleFile;

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
  public void sinkProjectTest() throws Exception {
    String projectName = RandomStringUtils.randomAlphabetic(7);
    ProjectEvent projectEvent = createProjectEvent(projectName);
    // add rdf file
    FileEvent fileEvent = addFileToProject(projectEvent.getId(), rdfExampleFile);
    sink(projectEvent, fileEvent, null);

    List<LogEvent> logEvents = getLogs(projectEvent);

    assertTrue(logEvents.stream()
                        .anyMatch(logEvent -> String.format("received sink response with status SUCCESS, for project %s",
                                                            projectEvent.getId())
                                                    .equals(logEvent.getMessage())));
    assertFalse(logEvents.stream()
                         .anyMatch(logEvent -> ERROR.equals(logEvent.getType())));
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
                        .anyMatch(logEvent -> String.format("received sink response with status SUCCESS, for project %s",
                                                            projectEvent.getId())
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

    try {
      restTemplate.exchange(String.format("%s/project/%s/shacl-validation?shapesFileId=%s&rdfModelFileId=%s", backendUrl,
                                          projectEvent.getId(), shaclShapes.getId(), badData.getId()), HttpMethod.GET,
                            testingUtils.requestWithEmptyBody(), String.class);
      fail("should throw an exception");
    }
    catch (HttpStatusCodeException exception) {
      assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
      String expectedMessage = exception.getResponseBodyAsString();
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
    // add rdf file
    log.info("add file {} to project {}", resource.getFilename(), projectId);
    ResponseEntity<ProjectEvent> response = testingUtils.postFileToProject(projectId, backendUrl + "/project/add-file", resource.getFilename(),
                                                                           IOUtils.toByteArray(resource.getInputStream()));
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
}
