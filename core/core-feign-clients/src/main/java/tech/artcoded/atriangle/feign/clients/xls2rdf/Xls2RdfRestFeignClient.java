package tech.artcoded.atriangle.feign.clients.xls2rdf;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface Xls2RdfRestFeignClient {
  /**
   * @param sourceString   type of source ("file", "url")
   * @param file           uploaded file if source=file
   * @param language       language of the labels to generate
   * @param url            URL of the file if source=url
   * @param format         output format of the generated files
   * @param useskosxl      flag to generate SKOS-XL or not
   * @param useZip         flag to output result in a ZIP file or not
   * @param useGraph       flag to indicate if graph files should be generated or not
   * @param ignorePostProc flag to indicate if graph files should be generated or not
   * @return
   * @throws Exception
   */
  @RequestMapping(value = "/convert",
                  method = RequestMethod.POST,
                  consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<ByteArrayResource> convertRDF(
    @RequestParam(value = "source") String sourceString, // FILE or URL
    @RequestPart(value = "file",
                 required = false) MultipartFile file,
    @RequestParam(value = "language",
                  required = false) String language,
    @RequestParam(value = "url",
                  required = false) String url,
    @RequestParam(value = "output",
                  required = false) String format,
    @RequestParam(value = "useskosxl",
                  required = false) boolean useskosxl,
    @RequestParam(value = "usezip",
                  required = false) boolean useZip,
    @RequestParam(value = "usegraph",
                  required = false) boolean useGraph,
    @RequestParam(value = "ignorePostProc",
                  required = false) boolean ignorePostProc
  ) throws Exception;

  @RequestMapping(method = RequestMethod.GET,
                  value = "/rest/ping")
  ResponseEntity<Map<String, String>> ping();

}
