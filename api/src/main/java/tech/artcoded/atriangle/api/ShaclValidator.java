package tech.artcoded.atriangle.api;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.system.RiotLib;
import org.topbraid.shacl.validation.ValidationUtil;

public interface ShaclValidator {
  Model getShapes();

  default Model validate(Model modelToValidate) {
    org.apache.jena.rdf.model.Resource report = ValidationUtil.validateModel(modelToValidate, getShapes(), true);
    Model model = report.getModel();
    Model m = ModelFactory.createDefaultModel();
    model.listStatements().toList()
         .stream()
         .map(statement -> {
           if (statement.getSubject().isAnon()) {
             String node = RiotLib.blankNodeToIriString(statement.getSubject().asNode());
             return ResourceFactory.createStatement(ResourceFactory.createProperty(node), statement.getPredicate(), statement.getObject());
           }
           return statement;
         })
         .forEach(m::add);

    return m;

  }
}