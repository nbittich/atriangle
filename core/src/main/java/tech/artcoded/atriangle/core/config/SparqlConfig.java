package tech.artcoded.atriangle.core.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.artcoded.atriangle.api.SimpleSparqlService;

import javax.inject.Named;

import static tech.artcoded.atriangle.core.config.NamedBean.SIMPLE_SPARQL_SERVICE;

@Configuration
@Slf4j
public class SparqlConfig {

  @Value("${sparql.endpoint.url}")
  private String sparqlEndpointUrl;

  @Bean(destroyMethod = "close")
  @Named(SIMPLE_SPARQL_SERVICE)
  public SimpleSparqlService simpleSparqlService() {
    return () -> RDFConnectionFactory.connect(sparqlEndpointUrl, sparqlEndpointUrl, sparqlEndpointUrl);
  }
}