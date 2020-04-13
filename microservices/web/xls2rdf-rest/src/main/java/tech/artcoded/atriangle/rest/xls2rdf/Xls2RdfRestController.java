package tech.artcoded.atriangle.rest.xls2rdf;


import fr.sparna.rdf.xls2rdf.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.core.rest.annotation.SwaggerHeaderAuthentication;
import tech.artcoded.atriangle.core.rest.controller.BuildInfoControllerTrait;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.core.rest.util.RestUtil;
import tech.artcoded.atriangle.feign.clients.xls2rdf.Xls2RdfRestFeignClient;

import javax.inject.Inject;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@Slf4j
public class Xls2RdfRestController implements PingControllerTrait, BuildInfoControllerTrait, Xls2RdfRestFeignClient {
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
  @SwaggerHeaderAuthentication
  public ResponseEntity<ByteArrayResource> convertRDF(String sourceString, MultipartFile file, String language,
                                                      String url,
                                                      String format, boolean useSkosXl, boolean useZip,
                                                      boolean useGraph, boolean ignorePostProc) throws Exception {
    SOURCE_TYPE source = SOURCE_TYPE.valueOf(sourceString.toUpperCase());
    RDFFormat theFormat = RDFWriterRegistry.getInstance()
                                           .getFileFormatForMIMEType(format)
                                           .orElse(RDFFormat.RDFXML);

    InputStream in;
    String resultFileName = "skos-play-convert";

    switch (source) {
      case FILE: {
        if (file.isEmpty()) {
          throw new RuntimeException("Uploaded file is empty");
        }

        in = file.getInputStream();
        // set the output file name to the name of the input file
        resultFileName = (file.getOriginalFilename()
                              .contains(".")) ? file.getOriginalFilename()
                                                    .substring(0, file.getOriginalFilename()
                                                                      .lastIndexOf('.')) : file.getOriginalFilename();
        break;
      }
      case URL: {
        if (url.isEmpty()) {
          throw new RuntimeException("Uploaded link file is empty");
        }

        try {
          URL urls = new URL(url);
          InputStream urlInputStream = urls.openStream(); // throws an IOException
          in = new DataInputStream(new BufferedInputStream(urlInputStream));

          // set the output file name to the final part of the URL
          resultFileName = (!urls.getPath()
                                 .equals("")) ? urls.getPath() : resultFileName;
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
      resultFileName = (resultFileName.contains(".")) ? resultFileName.substring(0, resultFileName.lastIndexOf('.')) : resultFileName;
      String extension = (useZip) ? "zip" : theFormat.getDefaultFileExtension();

      // add the date in the filename
      String dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

      ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();
      try (var openedIn = in) {
        List<String> cvIds = runConversion(
          new ModelWriterFactory(useZip, theFormat, useGraph).buildNewModelWriter(responseOutputStream),
          openedIn,
          Optional.ofNullable(language)
                  .filter(StringUtils::isNotEmpty)
                  .orElse(null),
          useSkosXl,
          ignorePostProc
        );

        Collections.sort(cvIds);
        cvIds.stream()
             .map(cv -> "ConvertedVocabularyIdentifier: " + cv)
             .forEach(log::info);

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
                                     InputStream fileFrom,
                                     String lang,
                                     boolean generateXl,
                                     boolean ignorePostProc) {
    Xls2RdfConverter converter = new Xls2RdfConverter(writer, lang);
    List<Xls2RdfPostProcessorIfc> postProcessors = new ArrayList<>();

    if (!ignorePostProc) {
      postProcessors.add(new SkosPostProcessor());
      if (generateXl) {
        postProcessors.add(new SkosXlPostProcessor(true, true));
      }
    }
    converter.setPostProcessors(postProcessors);
    converter.processInputStream(fileFrom);
    return converter.getConvertedVocabularyIdentifiers();
  }
}
