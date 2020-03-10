package tech.artcoded.atriangle.api;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

public interface SimpleSparqlService {
  RDFConnection rdfConnection();

  Logger LOGGER = LoggerFactory.getLogger(SimpleSparqlService.class);

  Function<String, String> CLEAR_GRAPH_QUERY = graphUri -> String.format("CLEAR GRAPH <%s>", graphUri);
  Function<String, String> ASK_GRAPH_QUERY = graphUri -> String.format("ask where {graph <%s> {?s ?p ?o }}", graphUri);

  default Model construct(String query) {
    return delegateTransformer(conn -> conn.queryConstruct(query));
  }

  default void select(String query, Consumer<QuerySolution> rowAction) {
    delegateConsumer(conn -> conn.querySelect(query, rowAction));
  }

  default void update(String query) {
    delegateConsumer(conn -> conn.update(query));
  }

  default void update(UpdateRequest updateRequest) {
    delegateConsumer(conn -> conn.update(updateRequest));
  }

  default boolean ask(String query) {
    return delegateTransformer(conn -> conn.queryAsk(query));
  }

  default void deleteGraph(String graphUri) {
    delegateConsumer(conn -> conn.delete(graphUri));
  }

  default void load(String graphUri, Model model) {
    delegateConsumer(conn -> conn.load(graphUri, model));
  }

  default boolean askForGraph(String graphUri) {
    return ask(ASK_GRAPH_QUERY.apply(graphUri));
  }

  default <T> T delegateTransformer(CheckedFunction<RDFConnection, T> delegateMethod) {
    return delegateMethod.safeExecute(rdfConnection());
  }

  default void delegateConsumer(CheckedConsumer<RDFConnection> delegateMethod) {
    delegateMethod.safeConsume(rdfConnection());
  }

  default UpdateRequest updateRequest(String... queries) {
    var updateRequest = UpdateFactory.create();
    Arrays.stream(queries)
          .filter(String::isEmpty)
          .forEach(updateRequest::add);
    return updateRequest;
  }

  default void close() {
    rdfConnection().close();
  }

  default void ping() {
    Model model = this.construct("select distinct ?Concept where {[] a ?Concept} LIMIT 1");
    LOGGER.info("Virtuoso running test result: {}", ModelConverter.modelToLang(model, Lang.JSONLD));
  }
}
