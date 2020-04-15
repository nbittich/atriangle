package tech.artcoded.atriangle.core.sparql;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.repository.util.RepositoryUtil;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.JSONLDMode;
import org.openrdf.rio.helpers.JSONLDSettings;
import org.openrdf.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.artcoded.atriangle.api.CheckedSupplier;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public interface ModelConverter {

  Logger LOG = LoggerFactory.getLogger(ModelConverter.class);

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

  static boolean equals(Model firstModel, Model secondModel) {
    return ModelUtil.equals(firstModel, secondModel);
  }
  static Collection<? extends Statement> difference(Model firstModel, Model secondModel) {
    return RepositoryUtil.difference(firstModel, secondModel);
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

  static String inputStreamToLang(String filename, CheckedSupplier<InputStream> file, RDFFormat lang) {
    return modelToLang(inputStreamToModel(filename, file), lang);
  }

  static Model inputStreamToModel(String filename, CheckedSupplier<InputStream> file) {
    RDFFormat rdfFormat = RDFFormat.forFileName(filename, RDFFormat.TURTLE);
    return toModel(file, rdfFormat);
  }

  static boolean checkFileFormat(String filename) {
    try{
      return RDFFormat.forFileName(filename) != null;
    }catch (Exception e){
      LOG.error("an error occurred", e);
    }
    return false;
  }
}
