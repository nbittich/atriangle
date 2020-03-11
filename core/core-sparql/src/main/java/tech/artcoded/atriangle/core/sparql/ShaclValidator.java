package tech.artcoded.atriangle.core.sparql;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.shacl.ShaclSail;
import org.eclipse.rdf4j.sail.shacl.ShaclSailValidationException;
import org.eclipse.rdf4j.sail.shacl.results.ValidationReport;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public interface ShaclValidator {

  @SneakyThrows
  default Optional<ValidationReport> validate(String turtleModel, String turtleShaclRules) {
    ShaclSail shaclSail = new ShaclSail(new MemoryStore());
    SailRepository sailRepository = new SailRepository(shaclSail);
    sailRepository.init();
    try (SailRepositoryConnection connection = sailRepository.getConnection();
         InputStream shaclIs = IOUtils.toInputStream(turtleShaclRules, StandardCharsets.UTF_8)) {
      connection.begin();
      connection.add(shaclIs, "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);
      connection.commit();
      // load data
      connection.begin();
      try (InputStream is = IOUtils.toInputStream(turtleModel, StandardCharsets.UTF_8)) {
        connection.add(is, "", RDFFormat.TURTLE);
      }
      connection.commit();
      return Optional.empty();
    }
    catch (RepositoryException exception) {
      Throwable cause = exception.getCause();
      if (cause instanceof ShaclSailValidationException) {
        ValidationReport validationReport = ((ShaclSailValidationException) cause).getValidationReport();
        return Optional.of(validationReport);
      }
      throw exception;
    }
  }
}
