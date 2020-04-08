package tech.artcoded.atriangle.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tech.artcoded.atriangle.api.CheckedThreadHelper;
import tech.artcoded.atriangle.api.dto.FileEvent;
import tech.artcoded.atriangle.api.dto.LogEvent;
import tech.artcoded.atriangle.api.dto.ProjectEvent;
import tech.artcoded.atriangle.api.dto.SinkRequest;

import javax.inject.Inject;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;

@Component
@Slf4j
public class BasicScenario implements CommandLineRunner {
  private final RestTemplate restTemplate;
  private final ObjectMapper mapper;
  private final TestingUtils testingUtils;
  @Value("${backend.url}")
  private String backendUrl;

  @Value("classpath:rdf-example.rdf")
  private Resource rdfExampleFile;

  @Inject
  public BasicScenario(RestTemplate restTemplate,
                       ObjectMapper mapper, TestingUtils testingUtils) {
    this.restTemplate = restTemplate;
    this.mapper = mapper;
    this.testingUtils = testingUtils;
  }

  @Override
  public void run(String... args) throws Exception {
    // create project
    log.info("project creation");
    ProjectEvent projectEvent = restTemplate.postForObject(backendUrl + "/project?name=" + RandomStringUtils.randomAlphabetic(7)
                                                                                                            .toLowerCase(), testingUtils.requestWithEmptyBody(), ProjectEvent.class);
    log.info("project with id {} and name {} created", projectEvent.getId(), projectEvent.getName());
    // add rdf file
    log.info("add rdf example");
    ResponseEntity<ProjectEvent> response = testingUtils.postFileToProject(projectEvent.getId(), backendUrl + "/project/add-file", rdfExampleFile.getFilename(), IOUtils.toByteArray(rdfExampleFile.getInputStream()));
    log.info("response status {}", response.getStatusCodeValue());
    projectEvent = response.getBody();
    Preconditions.checkArgument(projectEvent.getFileEvents()
                                            .size() == 1, "file not uploaded");
    FileEvent fileEvent = projectEvent.getFileEvents()
                                      .get(0);
    log.info("file event: {}", fileEvent);
    // sink
    log.info("sink now");
    SinkRequest sinkRequest = SinkRequest.builder()
                                         .projectId(projectEvent.getId())
                                         .rdfFileEventId(fileEvent.getId())
                                         .build();
    restTemplate.postForLocation(backendUrl + "/project/sink", testingUtils.requestWithBody(sinkRequest));
    // log output
    CheckedThreadHelper.FIVE_SECONDS.sleep();
    log.info("check logs");
    LogEvent[] logEvents = restTemplate.getForObject(backendUrl + "/project/" + projectEvent.getId() + "/logs", LogEvent[].class);
    log.info("logs: \n{}", mapper.writerWithDefaultPrettyPrinter()
                                 .writeValueAsString(Arrays.asList(requireNonNull(logEvents))));
    log.info("done");
    System.exit(0);
  }

}
