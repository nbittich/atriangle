package tech.artcoded.atriangle.api;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public interface SparqlService {
  Logger LOGGER = LoggerFactory.getLogger(SparqlService.class);
  String GRAPH_SUFFIX = "/graph";

  Function<String, String> CONSTRUCT_GRAPH_URI = uri -> uri + GRAPH_SUFFIX;
  Function<String, String> CLEAR_GRAPH_QUERY = graphUri -> String.format("CLEAR GRAPH <%s>", graphUri);
  Function<String, String> ASK_GRAPH_QUERY = graphUri -> String.format("ask where {graph <%s> {?s ?p ?o }}", graphUri);

  BiFunction<String, Model, String> INSERT_GRAPH_QUERY = (graphUri, model) -> {
    var writer = new StringWriter();
    model.write(writer, "ttl");

    return String.format("INSERT DATA { GRAPH <%s> { %s } }", graphUri, writer.toString());
  };

  SparqlServiceParam params();

  default String constructGraphUri(String uri) {
    return CONSTRUCT_GRAPH_URI.apply(uri);
  }

  default void insert(InputStream is, String rdfLang) {
    insert(params().getDefaultGraphUri(), is, rdfLang, null);
  }

  default void insert(String graphUri, InputStream is, String rdfLang, String base) {
    var model = ModelFactory.createDefaultModel();
    model.read(is, base, rdfLang);
    insertOrUpdateToGraph(graphUri, model);
  }

  default Model construct(String query) {
    return queryExecution(query, QueryExecution::execConstruct);
  }

  @SneakyThrows
  default Model construct(InputStream query) {
    return queryExecution(IOUtils.toString(query, StandardCharsets.UTF_8), QueryExecution::execConstruct);
  }

  default void updateExecution(UpdateRequest updateRequest) {
    updateExecution(updateRequest, UpdateProcessor::execute);
  }

  default boolean ask(String askQuery) {
    return queryExecution(askQuery, QueryExecution::execAsk);
  }

  default boolean askGraph(String graphUri) {
    return ask(ASK_GRAPH_QUERY.apply(graphUri));
  }

  default void clearGraph(String graphUri) {
    var query = CLEAR_GRAPH_QUERY.apply(graphUri);
    var request = updateRequest(query);
    updateExecution(request);
  }

  default UpdateRequest updateRequest(String... queries) {
    var updateRequest = UpdateFactory.create();
    Arrays.stream(queries)
          .filter(String::isEmpty)
          .forEach(updateRequest::add);
    return updateRequest;
  }

  default void insertOrUpdateToGraph(String graphUri, Model model) {
    String clearGraphQuery = CLEAR_GRAPH_QUERY.apply(graphUri);
    String insertQuery = INSERT_GRAPH_QUERY.apply(graphUri, model);
    UpdateRequest updateRequest = updateRequest(clearGraphQuery, insertQuery);
    updateExecution(updateRequest);
  }

  @SneakyThrows
  default <T> T queryExecution(String query, Function<QueryExecution, T> queryExecutionConsumer) {
    try (
      var queryExecution = QueryExecutionFactory.sparqlService(params().getSparqlEndpointUrl(), query, params().getHttpClient())) {
      return queryExecutionConsumer.apply(queryExecution);
    }
  }

  @SneakyThrows
  default void updateExecution(UpdateRequest updateRequest, Consumer<UpdateProcessor> consumer) {
    UpdateProcessor remoteForm = UpdateExecutionFactory.createRemoteForm(updateRequest, params().getSparqlEndpointUrl(), params().getHttpClient());
    consumer.accept(remoteForm);
  }

  default void ping() {
    Model model = this.construct("select distinct ?Concept where {[] a ?Concept} LIMIT 1");
    LOGGER.info("Virtuoso running test result: {}", ModelUtils.modelToLang(model, Lang.JSONLD));
  }
}
