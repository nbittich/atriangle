package tech.artcoded.atriangle.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tech.artcoded.atriangle.core.rest.util.RestUtil;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public interface TestingUtils {
  ObjectMapper MAPPER = new ObjectMapper();

  TestRestTemplate restTemplate();

  default HttpEntity<String> requestWithEmptyBody() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(headers);
  }

  default ResponseEntity<ByteArrayResource> downloadFile(String url) {
    return restTemplate().execute(url, HttpMethod.GET, null, clientHttpResponse -> {
      String filename = clientHttpResponse.getHeaders()
                                          .getContentDisposition()
                                          .getFilename();
      String contentType = requireNonNull(clientHttpResponse.getHeaders()
                                                            .getContentType())
        .toString();
      return RestUtil.transformToByteArrayResource(filename, contentType, IOUtils.toByteArray(clientHttpResponse.getBody()));
    });
  }

  @SneakyThrows
  default HttpEntity<String> requestWithBody(Object requestBody) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(MAPPER.writeValueAsString(requestBody), headers);
  }

  default <T> ResponseEntity<T> postFileToProject(Map<String, String> requestParams, String url, String filename,
                                                  Resource resource, Class<T> tClass) {
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
    requestParams.forEach(body::add);

    HttpEntity<MultiValueMap<String, Object>> requestEntity =
      new HttpEntity<>(body, headers);
    return restTemplate().exchange(url,
                                   HttpMethod.POST,
                                   requestEntity,
                                   tClass);
  }
}
