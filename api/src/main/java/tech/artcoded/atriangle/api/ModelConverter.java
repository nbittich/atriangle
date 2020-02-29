package tech.artcoded.atriangle.api;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.elasticsearch.common.Strings;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public interface ModelConverter {
  static String modelToLang(Model model, Lang lang) {
    if (model.isEmpty()) throw new RuntimeException("model cannot be empty");

    StringWriter sw = new StringWriter();
    model.write(sw, lang.getLabel());
    return sw.toString();
  }

  @SneakyThrows
  static Model toModel(String value, Lang lang) {
    if (Strings.isEmpty(value)) throw new RuntimeException("model cannot be empty");
    Model defaultModel = ModelFactory.createDefaultModel();
    defaultModel.read(IOUtils.toInputStream(value, StandardCharsets.UTF_8), null, lang.getLabel());
    return defaultModel;
  }
}
