package tech.artcoded.atriangle.api;

import org.apache.commons.io.IOUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.elasticsearch.common.Strings;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public interface ModelConverter {
  static String modelToLang(Model model, Lang lang) {
    if (model.isEmpty()) throw new RuntimeException("model cannot be empty");

    StringWriter sw = new StringWriter();
    model.write(sw, lang.getLabel());
    return sw.toString();
  }

  static Model toModel(String value, Lang lang) {
    if (Strings.isEmpty(value)) throw new RuntimeException("model cannot be empty");
    return toModel(() -> IOUtils.toInputStream(value, StandardCharsets.UTF_8), lang);
  }

  static Model toModel(CheckedSupplier<InputStream> is, Lang lang) {
    Model defaultModel = ModelFactory.createDefaultModel();
    defaultModel.read(is.safeGet(), null, lang.getLabel());
    return defaultModel;
  }

  static OntModel createOntModel(String filePath, Lang lang) {
    OntModel ontoModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    try (InputStream in = FileManager.get()
                                     .open(filePath)) {
      RDFDataMgr.read(ontoModel, in, lang);
    }
    catch (Exception je) {
      throw new RuntimeException(je);
    }
    return ontoModel;
  }

  static String inputStreamToLang(String fileExtension, CheckedSupplier<InputStream> file, Lang lang) {

    return modelToLang(inputStreamToModel(fileExtension, file), lang);
  }

  static Model inputStreamToModel(String fileExtension, CheckedSupplier<InputStream> file) {
    switch (fileExtension) {
      case "ttl":
        return toModel(file, Lang.TURTLE);
      case "rdf":
        return toModel(file, Lang.RDFXML);
      case "trig":
        return toModel(file, Lang.TRIG);
      case "json":
        return toModel(file, Lang.JSONLD);
      default:
        throw new RuntimeException("Lang not supported yet");
    }
  }
}
