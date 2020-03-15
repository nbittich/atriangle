package tech.artcoded.atriangle.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@FunctionalInterface
public interface HttpClientTemplate {
  String serverUrl();

  HttpClient.Builder httpClient = HttpClient.newBuilder()
                                            .version(HttpClient.Version.HTTP_2)
                                            .connectTimeout(Duration.ofMinutes(2));


  default String encodeBase64(String toEncode) {
    return Base64.getEncoder()
                 .encodeToString(toEncode.getBytes());
  }

  default HttpResponse<String> httpRequest(String relativePath,
                                           String httpMethod,
                                           String body,
                                           Function<HttpClient.Builder, HttpClient.Builder> customHttpClient,
                                           Function<HttpRequest.Builder, HttpRequest> customRequest) throws IOException, InterruptedException {
    return httpRequest(createHttpRequest(relativePath, httpMethod, body, customRequest), customHttpClient.apply(httpClient));
  }

  default CompletableFuture<HttpResponse<String>> httpAsyncRequest(String relativePath,
                                                                   String httpMethod,
                                                                   String body,
                                                                   Function<HttpClient.Builder, HttpClient.Builder> customHttpClient,
                                                                   Function<HttpRequest.Builder, HttpRequest> customRequest) throws IOException, InterruptedException {
    return httpAsyncRequest(createHttpRequest(relativePath, httpMethod, body, customRequest), customHttpClient.apply(httpClient));
  }

  default CompletableFuture<HttpResponse<String>> httpAsyncRequest(HttpRequest request,
                                                                   HttpClient.Builder builder) throws IOException, InterruptedException {
    return builder.build()
                  .sendAsync(request, HttpResponse.BodyHandlers.ofString());
  }

  default HttpResponse<String> httpRequest(HttpRequest request,
                                           HttpClient.Builder builder) throws IOException, InterruptedException {
    return builder.build()
                  .send(request, HttpResponse.BodyHandlers.ofString());
  }

  default HttpRequest createHttpRequest(String relativePath,
                                        String httpMethod,
                                        String body,
                                        Function<HttpRequest.Builder, HttpRequest> customRequest) {
    return customRequest.apply(HttpRequest.newBuilder()
                                          .uri(URI.create(urlFor(relativePath)))
                                          .method(httpMethod.toUpperCase(), isEmpty(body) ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers
                                            .ofString(body)))
      ;
  }

  default HttpRequest createHttpRequest(String serverUrl,
                                        String relativePath,
                                        String httpMethod,
                                        String body,
                                        Function<HttpClient.Builder, HttpClient.Builder> customHttpClient,
                                        Function<HttpRequest.Builder, HttpRequest> customRequest) {
    return customRequest.apply(HttpRequest.newBuilder()
                                          .uri(URI.create(serverUrl + relativePath))
                                          .method(httpMethod.toUpperCase(), isEmpty(body) ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers
                                            .ofString(body)))
      ;
  }

  default HttpRequest createHttpRequest(String relativePath,
                                        String httpMethod,
                                        String body) {
    return HttpRequest.newBuilder()
                      .uri(URI.create(urlFor(relativePath)))
                      .method(httpMethod.toUpperCase(), isEmpty(body) ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers
                        .ofString(body))
                      .build();

  }

  default HttpRequest.Builder createBasicAuthorizationHeader(HttpRequest.Builder builder, String username,
                                                             String password) {
    return builder.header("Authorization", "Basic " + encodeBase64(username + ":" + password));
  }

  default HttpRequest.Builder createAuthTokenHeader(HttpRequest.Builder builder, String accessToken) {
    return builder.header("X-Auth-Token", accessToken);
  }


  default String urlFor(String relativePath) {
    return serverUrl() + relativePath;
  }


}
