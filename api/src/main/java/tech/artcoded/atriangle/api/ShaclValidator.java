package tech.artcoded.atriangle.api;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.system.RiotLib;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

import java.io.InputStream;
import java.util.Optional;

public interface ShaclValidator {
  Model getShapes();

  static Optional<Model> validateModel(Model inputModel, boolean shaclEnabled, String extension,
                                       CheckedSupplier<InputStream> shaclInputStream) {

    if (!shaclEnabled) return Optional.empty();

    ShaclValidator validator = () -> ModelConverter.inputStreamToModel(extension, shaclInputStream);

    Model validationModel = validator.validate(inputModel);

    StmtIterator isConforms = validationModel.listStatements(null, SH.conforms, (RDFNode) null);
    boolean conform = isConforms.hasNext() && isConforms.nextStatement()
                                                        .getBoolean();
    if (!conform) {
      return Optional.of(validationModel);
    }
    return Optional.empty();
  }

  default Model validate(Model modelToValidate) {
    org.apache.jena.rdf.model.Resource report = ValidationUtil.validateModel(modelToValidate, getShapes(), true);
    Model model = report.getModel();
    Model m = ModelFactory.createDefaultModel();
    model.listStatements()
         .toList()
         .stream()
         .map(statement -> {
           if (statement.getSubject()
                        .isAnon()) {
             String node = RiotLib.blankNodeToIriString(statement.getSubject()
                                                                 .asNode());
             return ResourceFactory.createStatement(ResourceFactory.createProperty(node), statement.getPredicate(), statement.getObject());
           }
           return statement;
         })
         .forEach(m::add);

    return m;

  }
}
