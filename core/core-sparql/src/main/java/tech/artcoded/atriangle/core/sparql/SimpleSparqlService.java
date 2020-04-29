package tech.artcoded.atriangle.core.sparql;

import com.bigdata.rdf.sail.webapp.SD;
import com.bigdata.rdf.sail.webapp.client.ConnectOptions;
import com.bigdata.rdf.sail.webapp.client.JettyResponseListener;
import com.bigdata.rdf.sail.webapp.client.RemoteRepository;
import com.bigdata.rdf.sail.webapp.client.RemoteRepositoryManager;
import lombok.SneakyThrows;
import org.openrdf.model.Statement;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.artcoded.atriangle.api.CheckedConsumer;
import tech.artcoded.atriangle.api.PropertyStore;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface SimpleSparqlService {

  Function<String, PropertyStore> CREATE_NAMESPACE_PROPERTIES =
      ns ->
          () ->
              Map.of(
                  "com.bigdata.rdf.sail.namespace",
                  ns,
                  "com.bigdata.rdf.store.AbstractTripleStore.axiomsClass",
                  "com.bigdata.rdf.axioms.NoAxioms",
                  "com.bigdata.rdf.store.AbstractTripleStore.quads",
                  "true",
                  "com.bigdata.rdf.store.AbstractTripleStore.statementIdentifiers",
                  "false",
                  "com.bigdata.rdf.sail.truthMaintenance",
                  "false",
                  "com.bigdata.rdf.store.AbstractTripleStore.textIndex",
                  "true");

  String getServiceUrl();

  RemoteRepositoryManager getRemoteRepositoryManager();

  Logger LOGGER = LoggerFactory.getLogger(SimpleSparqlService.class);

  @SneakyThrows
  default void createNamespace(String namespace) {
    if (!namespaceExists(namespace)) {
      LOGGER.info("namespace {} does not exist", namespace);
      LOGGER.info("Create namespace {}...", namespace);
      getRemoteRepositoryManager()
          .createRepository(namespace, CREATE_NAMESPACE_PROPERTIES.apply(namespace).toProperties());
      LOGGER.info("Create namespace {} done", namespace);
    } else {
      LOGGER.info("Namespace {} already exists", namespace);
    }
  }

  @SneakyThrows
  default TupleQueryResult tupleQuery(String namespace, String query) {
    return getRemoteRepositoryManager()
        .getRepositoryForNamespace(namespace)
        .prepareTupleQuery(query)
        .evaluate();
  }

  @SneakyThrows
  default boolean booleanQuery(String namespace, String query) {
    return getRemoteRepositoryManager()
        .getRepositoryForNamespace(namespace)
        .prepareBooleanQuery(query)
        .evaluate();
  }

  @SneakyThrows
  default GraphQueryResult graphQuery(String namespace, String query) {
    return getRemoteRepositoryManager()
        .getRepositoryForNamespace(namespace)
        .prepareGraphQuery(query)
        .evaluate();
  }

  @SneakyThrows
  default void deleteNamespace(String namespace) {
    if (namespaceExists(namespace)) {
      LOGGER.info("Delete namespace {}...", namespace);
      getRemoteRepositoryManager().deleteRepository(namespace);
    } else {
      LOGGER.info("Namespace {} does not exist", namespace);
    }
  }

  default void close() throws Exception {
    getRemoteRepositoryManager().close();
  }

  @SneakyThrows
  default JettyResponseListener getNamespaceProperties(String namespace) {

    final ConnectOptions opts =
        new ConnectOptions(getServiceUrl() + "/namespace/" + namespace + "/properties");
    opts.method = "GET";
    return getRemoteRepositoryManager().doConnect(opts);
  }

  @SneakyThrows
  default boolean namespaceExists(String namespace) {
    GraphQueryResult res = null;
    try {
      res = getRemoteRepositoryManager().getRepositoryDescriptions();
      while (res.hasNext()) {
        final Statement stmt = res.next();
        if (stmt.getPredicate().toString().equals(SD.KB_NAMESPACE.stringValue())) {
          if (namespace.equals(stmt.getObject().stringValue())) {
            return true;
          }
        }
      }
    } finally {
      Optional.ofNullable(res).ifPresent(CheckedConsumer.toConsumer(GraphQueryResult::close));
    }
    return false;
  }

  @SneakyThrows
  default void load(String namespace, InputStream resource, RDFFormat rdfFormat) {
    try (resource) {
      if (!namespaceExists(namespace)) {
        createNamespace(namespace);
      }
      getRemoteRepositoryManager()
          .getRepositoryForNamespace(namespace)
          .add(new RemoteRepository.AddOp(resource, rdfFormat));
    }
  }

  default JettyResponseListener getStatus() throws Exception {
    final ConnectOptions opts = new ConnectOptions(getServiceUrl() + "/status");
    opts.method = "GET";
    return getRemoteRepositoryManager().doConnect(opts);
  }
}
