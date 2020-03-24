package tech.artcoded.atriangle.rest.xls2rdf;


import fr.sparna.rdf.xls2rdf.*;
import lombok.Getter;
import org.apache.commons.lang.NotImplementedException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.controller.BuildInfoControllerTrait;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.core.rest.util.RestUtil;
import tech.artcoded.atriangle.feign.clients.xls2rdf.Xls2RdfRestFeignClient;

import javax.inject.Inject;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@CrossOriginRestController
public class Xls2RdfRestController implements PingControllerTrait, BuildInfoControllerTrait, Xls2RdfRestFeignClient {
  private Logger log = LoggerFactory.getLogger(this.getClass().getName());


  @Getter
  private final BuildProperties buildProperties;

  @Inject
  public Xls2RdfRestController(BuildProperties buildProperties) {
    this.buildProperties = buildProperties;
  }

  private enum SOURCE_TYPE {
    FILE,
    URL,
  }

  @Override
  public ResponseEntity<ByteArrayResource> convertRDF(String sourceString,MultipartFile file, String language,String url,
     String format, boolean useskosxl, boolean useZip, boolean useGraph, boolean ignorePostProc) throws Exception {
    SOURCE_TYPE source = SOURCE_TYPE.valueOf(sourceString.toUpperCase());
    RDFFormat theFormat = RDFWriterRegistry.getInstance().getFileFormatForMIMEType(format).orElse(RDFFormat.RDFXML);

    /**************************CONVERSION RDF**************************/
    InputStream in = null;
    String resultFileName = "skos-play-convert";

    switch (source) {
      case FILE: {
        log.debug("*Conversion à partir d'un fichier uploadé : " + file.getOriginalFilename());
        if (file.isEmpty()) {
          throw new RuntimeException("Uploaded file is empty");
        }

        in = file.getInputStream();
        // set the output file name to the name of the input file
        resultFileName = (file.getOriginalFilename().contains(".")) ? file.getOriginalFilename()
                                                                          .substring(0, file.getOriginalFilename()
                                                                                            .lastIndexOf('.')) : file.getOriginalFilename();
        break;
      }
      case URL: {
        log.debug("*Conversion à partir d'une URL : " + url);
        if (url.isEmpty()) {
          throw new RuntimeException("Uploaded link file is empty");
        }

        try {
          URL urls = new URL(url);
          InputStream urlInputStream = urls.openStream(); // throws an IOException
          in = new DataInputStream(new BufferedInputStream(urlInputStream));

          // set the output file name to the final part of the URL
          resultFileName = (!urls.getPath().equals("")) ? urls.getPath() : resultFileName;
          // keep only latest file, after final /
          resultFileName = (resultFileName.contains("/")) ? resultFileName.substring(0, resultFileName.lastIndexOf("/")) : resultFileName;
        }
        catch (IOException e) {
          log.error("error", e);
          throw new RuntimeException(e);
        }

        break;
      }
      default:
        throw new NotImplementedException();
    }

    try {
      log.debug("*Lancement de la conversion avec lang=" + language + " et usexl=" + useskosxl);
      resultFileName = (resultFileName.contains(".")) ? resultFileName.substring(0, resultFileName.lastIndexOf('.')) : resultFileName;
      String extension = (useZip) ? "zip" : theFormat.getDefaultFileExtension();

      // add the date in the filename
      String dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

      ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();
      try (var openedIn = in) {
        List<String> identifiant = runConversion(
          new ModelWriterFactory(useZip, theFormat, useGraph).buildNewModelWriter(responseOutputStream),
          openedIn,
          language.equals("") ? null : language,
          useskosxl,
          ignorePostProc
        );

        // sort to garantee order
        List<String> uri = new ArrayList<String>(identifiant);
        Collections.sort(uri);

        String filename = String.format("%s-%s.%s", resultFileName, dateString, extension);
        String contentType = useZip ? "application/zip" : theFormat.getDefaultMIMEType();
        return RestUtil.transformToByteArrayResource(filename, contentType, responseOutputStream.toByteArray());
      }
    }
    catch (Exception e) {
      log.error("error", e);
      throw new RuntimeException(e);
    }
  }

  private List<String> runConversion(ModelWriterIfc writer,
                                     InputStream filefrom,
                                     String lang,
                                     boolean generatexl,
                                     boolean ignorePostProc) {
    Xls2RdfConverter converter = new Xls2RdfConverter(writer, lang);
    List<Xls2RdfPostProcessorIfc> postProcessors = new ArrayList<>();

    if (!ignorePostProc) {
      postProcessors.add(new SkosPostProcessor());
    }
    if (!ignorePostProc && generatexl) {
      postProcessors.add(new SkosXlPostProcessor(generatexl, generatexl));
    }
    converter.setPostProcessors(postProcessors);

    converter.processInputStream(filefrom);
    return converter.getConvertedVocabularyIdentifiers();
  }
}
