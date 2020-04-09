package tech.artcoded.atriangle.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import tech.artcoded.atriangle.api.CheckedThreadHelper;
import tech.artcoded.atriangle.api.dto.*;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestConfig.class)
@Slf4j
@EnabledIfSystemProperty(named = "testsuite", matches = "integration")
public class ProjectTest {

  @Inject
  private RestTemplate restTemplate;

  @Inject
  private ObjectMapper mapper;

  @Inject
  private TestingUtils testingUtils;

  @Value("classpath:rdf-example.rdf")
  private Resource rdfExampleFile;

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
    sink(projectEvent, fileEvent);

    log.info("check logs");
    List<LogEvent> logEvents = Arrays.asList(requireNonNull(restTemplate.getForObject(backendUrl + "/project/" + projectEvent.getId() + "/logs", LogEvent[].class)));
    log.info("logs: \n{}", mapper.writerWithDefaultPrettyPrinter()
                                 .writeValueAsString(Arrays.asList(requireNonNull(logEvents))));

    assertFalse(logEvents.isEmpty());
    assertTrue(logEvents.stream()
                        .anyMatch(logEvent -> String.format("received sink response with status SUCCESS, for project %s",
                                                            projectEvent.getId())
                                                    .equals(logEvent.getMessage())));
    assertFalse(logEvents.stream()
                         .anyMatch(logEvent -> LogEventType.ERROR.equals(logEvent.getType())));
  }

  private void sink(ProjectEvent projectEvent, FileEvent fileEvent) {
    // sink
    log.info("sink now");
    SinkRequest sinkRequest = SinkRequest.builder()
                                         .projectId(projectEvent.getId())
                                         .rdfFileEventId(fileEvent.getId())
                                         .build();
    restTemplate.postForLocation(backendUrl + "/project/sink", testingUtils.requestWithBody(sinkRequest));
    // log output
    CheckedThreadHelper.FIVE_SECONDS.sleep();
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
    log.info("add rdf example");
    ResponseEntity<ProjectEvent> response = testingUtils.postFileToProject(projectId, backendUrl + "/project/add-file", resource.getFilename(),
                                                                           IOUtils.toByteArray(resource.getInputStream()));
    log.info("response status {}", response.getStatusCodeValue());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ProjectEvent updatedProjectEvent = response.getBody();
    assertNotNull(updatedProjectEvent);
    assertNotNull(updatedProjectEvent
                    .getFileEvents());
    assertEquals(1, updatedProjectEvent
      .getFileEvents()
      .size(), "file not uploaded");
    FileEvent fileEvent = updatedProjectEvent
      .getFileEvents()
      .get(0);
    assertNotNull(fileEvent);
    assertNotNull(fileEvent.getId());
    assertEquals(rdfExampleFile.getFilename(), fileEvent.getOriginalFilename());
    assertEquals(rdfExampleFile.getFilename(), fileEvent.getName());
    log.info("file event: {}", fileEvent);
    return fileEvent;
  }
}
