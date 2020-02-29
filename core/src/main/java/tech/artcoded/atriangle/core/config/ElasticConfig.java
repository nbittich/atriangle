package tech.artcoded.atriangle.core.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.artcoded.atriangle.api.ElasticSearchRdfService;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

import static tech.artcoded.atriangle.core.config.NamedBean.ELASTIC_CLIENT;

@Configuration
@Slf4j
public class ElasticConfig {

  @Value("${elasticsearch.hostname}")
  private String hostname;
  @Value("${elasticsearch.port}")
  private int port;
  @Value("${elasticsearch.scheme}")
  private String scheme;

  @Bean(destroyMethod = "close")
  @Named(ELASTIC_CLIENT)
  public RestHighLevelClient restHighLevelClient() {
    return new RestHighLevelClient(
      RestClient.builder(
        new HttpHost(hostname, port, scheme),
        new HttpHost(hostname, port, scheme)));
  }

  @Bean
  @Inject
  public ElasticSearchRdfService elasticSearchRdfService(
    @Named(ELASTIC_CLIENT) RestHighLevelClient client) throws IOException {
    ElasticSearchRdfService elasticSearchRdfService = () -> client;
    log.info("elasticsearch ping result: {}", elasticSearchRdfService.ping());
    MainResponse info = elasticSearchRdfService.info();
    log.info("clusterName: {}", info.getClusterName());
    log.info("clusterUuid: {}", info.getClusterUuid());
    log.info("nodeName: {}", info.getNodeName());
    log.info("elastic version: {}", info.getVersion()
                                        .getNumber());
    log.info("lucene version: {}", info.getVersion()
                                       .getLuceneVersion());
    log.info("tagLine: {}", info.getTagline());
    return elasticSearchRdfService;
  }
}
