package tech.artcoded.atriangle.core.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.artcoded.atriangle.api.SparqlService;
import tech.artcoded.atriangle.api.SparqlServiceParam;

import javax.inject.Inject;
import javax.inject.Named;

import static tech.artcoded.atriangle.core.config.NamedBean.VIRTUOSO_HTTP_CLIENT;

@Configuration
@Slf4j
public class VirtuosoConfig {

  @Value("${virtuoso.username}")
  private String username;
  @Value("${virtuoso.password}")
  private String password;
  @Value("${virtuoso.defaultGraph}")
  private String defaultGraphUri;

  @Value("${virtuoso.sparql.endpoint.url}")
  private String sparqlEndpointUrl;


  @Bean
  @Named(VIRTUOSO_HTTP_CLIENT)
  public HttpClient buildHttpClient() {
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
    return HttpClients.custom()
                      .setDefaultCredentialsProvider(credentialsProvider)
                      .build();
  }

  @Bean
  @Inject
  public SparqlService sparqlService(@Named(VIRTUOSO_HTTP_CLIENT) HttpClient client) {
    SparqlServiceParam sparqlServiceParam = new SparqlServiceParam(defaultGraphUri, sparqlEndpointUrl, client);
    log.info("sparql service params: {}", sparqlServiceParam.toString());
    SparqlService sparqlService = () -> sparqlServiceParam;
    sparqlService.ping();
    return sparqlService;
  }

}
