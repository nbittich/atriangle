package tech.artcoded.atriangle.api;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;

import java.io.StringWriter;

public interface ModelUtils {
  static String modelToLang(Model model, Lang lang) {
    if (model.isEmpty()) throw new RuntimeException("model cannot be empty");

    StringWriter sw = new StringWriter();
    model.write(sw, lang.getLabel());
    return sw.toString();
  }
}
