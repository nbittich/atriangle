package tech.artcoded.atriangle.api;

import lombok.Value;
import org.apache.http.client.HttpClient;

@Value
public class SparqlServiceParam {
  private String defaultGraphUri;

  private String sparqlEndpointUrl;

  private HttpClient httpClient;


}
