package tech.artcoded.atriangle.api;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.Model;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.RioConfig;
import org.openrdf.rio.WriterConfig;
import org.openrdf.rio.helpers.JSONLDMode;
import org.openrdf.rio.helpers.JSONLDSettings;
import org.openrdf.rio.helpers.StatementCollector;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public interface ModelConverter {

  @SneakyThrows
  static String modelToLang(Model model, RDFFormat lang) {
    if (model.isEmpty()) throw new RuntimeException("model cannot be empty");

    StringWriter sw = new StringWriter();
    WriterConfig writerConfig = new WriterConfig();

    config(writerConfig, lang);
    Rio.write(model, sw, lang, writerConfig);
    return sw.toString();
  }

  static Model toModel(String value, RDFFormat lang) {
    if (StringUtils.isEmpty(value)) throw new RuntimeException("model cannot be empty");
    return toModel(() -> IOUtils.toInputStream(value, StandardCharsets.UTF_8), lang);
  }

  @SneakyThrows
  static Model toModel(CheckedSupplier<InputStream> is, RDFFormat lang) {
    try (var stream = is.safeGet()) {

      Model graph = new TreeModel();
      StatementCollector collector = new StatementCollector(graph);
      RDFParser parser = Rio.createParser(lang);
      config(parser.getParserConfig(), lang);
      parser.setRDFHandler(collector);

      parser.parse(stream, "");
      return graph;
    }
  }

  static void config(RioConfig config, RDFFormat lang) {
    if (lang.equals(RDFFormat.JSONLD)) {
      config.set(JSONLDSettings.COMPACT_ARRAYS, true);
      config.set(JSONLDSettings.OPTIMIZE, true);
      config.set(JSONLDSettings.JSONLD_MODE, JSONLDMode.COMPACT);
    }
  }

  static String inputStreamToLang(String fileExtension, CheckedSupplier<InputStream> file, RDFFormat lang) {
    return modelToLang(inputStreamToModel(fileExtension, file), lang);
  }

  static Model inputStreamToModel(String fileExtension, CheckedSupplier<InputStream> file) {
    switch (fileExtension) {
      case "ttl":
        return toModel(file, RDFFormat.TURTLE);
      case "rdf":
        return toModel(file, RDFFormat.RDFXML);
      case "trig":
        return toModel(file, RDFFormat.TRIG);
      case "n3":
        return toModel(file, RDFFormat.N3);
      case "json":
        return toModel(file, RDFFormat.JSONLD);
      default:
        throw new RuntimeException("Lang not supported yet");
    }
  }
}
