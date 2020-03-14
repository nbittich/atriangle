package tech.artcoded.atriangle.rest.shacl;

import lombok.SneakyThrows;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.sparql.graph.GraphFactory;

import java.io.StringWriter;
import java.util.Optional;

public interface ShaclValidationUtils {

  @SneakyThrows
  static Optional<String> validate(String turtleModel, String turtleShaclRules) {
    StringWriter writer = new StringWriter();
    Graph shapesGraph = GraphFactory.createDefaultGraph();
    RDFParser.fromString(turtleShaclRules).base("").lang(Lang.TURTLE).build().parse(shapesGraph);
    Graph dataGraph = GraphFactory.createDefaultGraph();
    RDFParser.fromString(turtleModel).base("").lang(Lang.TURTLE).build().parse(dataGraph);

    Shapes shapes = Shapes.parse(shapesGraph);

    ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);

    if (report.conforms()) {
      return Optional.empty();
    }

    RDFDataMgr.write(writer, report.getModel(), Lang.TTL);
    return Optional.of(writer.toString());
  }
}
