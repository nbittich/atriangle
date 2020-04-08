package tech.artcoded.atriangle.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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


  @Value("${backend.url}")
  private String backendUrl;

  @Value("classpath:rdf-example.rdf")
  private Resource rdfExampleFile;

  @Inject
  public BasicScenario(RestTemplate restTemplate,
                       ObjectMapper mapper) {
    this.restTemplate = restTemplate;
    this.mapper = mapper;
  }

  @Override
  public void run(String... args) throws Exception {
    // create project
    log.info("project creation");
    ProjectEvent projectEvent = restTemplate.postForObject(backendUrl + "/project?name=" + RandomStringUtils.randomAlphabetic(7)
                                                                                                            .toLowerCase(), requestWithEmptyBody(), ProjectEvent.class);
    log.info("project with id {} and name {} created", projectEvent.getId(), projectEvent.getName());
    // add rdf file
    log.info("add rdf example");
    ResponseEntity<ProjectEvent> response = postFileToProject(projectEvent.getId(), backendUrl + "/project/add-file", rdfExampleFile.getFilename(), IOUtils.toByteArray(rdfExampleFile.getInputStream()));
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
    restTemplate.postForLocation(backendUrl + "/project/sink", requestWithBody(sinkRequest));
    // log output
    CheckedThreadHelper.FIVE_SECONDS.sleep();
    log.info("check logs");
    LogEvent[] logEvents = restTemplate.getForObject(backendUrl + "/project/" + projectEvent.getId() + "/logs", LogEvent[].class);
    log.info("logs: \n{}", mapper.writerWithDefaultPrettyPrinter()
                                 .writeValueAsString(Arrays.asList(requireNonNull(logEvents))));
    log.info("done");
    System.exit(0);
  }

  public HttpEntity<String> requestWithEmptyBody() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(headers);
  }

  @SneakyThrows
  public HttpEntity<String> requestWithBody(Object requestBody) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(mapper.writeValueAsString(requestBody), headers);
  }

  public ResponseEntity<ProjectEvent> postFileToProject(String projectId, String url, String filename, byte[] bytes) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    // This nested HttpEntiy is important to create the correct
    // Content-Disposition entry with metadata "name" and "filename"
    MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
    ContentDisposition contentDisposition = ContentDisposition
      .builder("form-data")
      .name("file")
      .filename(filename)
      .build();
    fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
    HttpEntity<byte[]> fileEntity = new HttpEntity<>(bytes, fileMap);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", fileEntity);
    body.add("projectId", projectId);

    HttpEntity<MultiValueMap<String, Object>> requestEntity =
      new HttpEntity<>(body, headers);
    return restTemplate.exchange(url,
                                 HttpMethod.POST,
                                 requestEntity,
                                 ProjectEvent.class);
  }
}
