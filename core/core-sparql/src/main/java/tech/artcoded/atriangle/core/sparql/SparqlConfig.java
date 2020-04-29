package tech.artcoded.atriangle.core.sparql;

import com.bigdata.rdf.sail.webapp.client.JettyResponseListener;
import com.bigdata.rdf.sail.webapp.client.RemoteRepositoryManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import javax.inject.Named;

@Configuration
@Slf4j
public class SparqlConfig {

  @Value("${sparql.endpoint.proxy.url}")
  private String sparqlEndpointUrl;

  @Inject private ConfigurableApplicationContext ctx;

  @Bean(destroyMethod = "close")
  @Named("coreSimpleSparqlService")
  public SimpleSparqlService simpleSparqlService() {
    try {
      SimpleSparqlServiceImpl simpleSparqlService =
          new SimpleSparqlServiceImpl(
              sparqlEndpointUrl, new RemoteRepositoryManager(sparqlEndpointUrl, false));
      JettyResponseListener status = simpleSparqlService.getStatus();
      log.info(
          "status for sparql endpoint {}, response body {}",
          status.getStatus(),
          status.getResponseBody());
      return simpleSparqlService;
    } catch (Exception e) {
      log.error("error during creation of sparql service", e);
      ctx.close();
      System.exit(-1);
      throw new RuntimeException(e);
    }
  }

  @lombok.Value
  static class SimpleSparqlServiceImpl implements SimpleSparqlService {
    private String serviceUrl;
    private RemoteRepositoryManager remoteRepositoryManager;
  }
}
