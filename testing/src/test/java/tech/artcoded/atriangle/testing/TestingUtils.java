package tech.artcoded.atriangle.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tech.artcoded.atriangle.api.dto.ProjectEvent;

public interface TestingUtils {
  ObjectMapper MAPPER = new ObjectMapper();

  RestTemplate restTemplate();

  default HttpEntity<String> requestWithEmptyBody() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(headers);
  }

  @SneakyThrows
  default HttpEntity<String> requestWithBody(Object requestBody) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(MAPPER.writeValueAsString(requestBody), headers);
  }

  default ResponseEntity<ProjectEvent> postFileToProject(String projectId, String url, String filename,
                                                         Resource resource) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
    ContentDisposition contentDisposition = ContentDisposition
      .builder("form-data")
      .name("file")
      .filename(filename)

      .build();
    fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
    HttpEntity<Resource> fileEntity = new HttpEntity<>(resource, fileMap);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", fileEntity);
    body.add("projectId", projectId);

    HttpEntity<MultiValueMap<String, Object>> requestEntity =
      new HttpEntity<>(body, headers);
    return restTemplate().exchange(url,
                                   HttpMethod.POST,
                                   requestEntity,
                                   ProjectEvent.class);
  }
}
