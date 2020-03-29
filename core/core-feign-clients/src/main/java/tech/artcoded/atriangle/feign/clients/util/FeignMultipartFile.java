package tech.artcoded.atriangle.feign.clients.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.MultipartFileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeignMultipartFile implements MultipartFile {
  private String name;
  private String originalFilename;
  private String contentType;
  private byte[] bytes;


  @Override
  public boolean isEmpty() {
    return MultipartFileUtils.isEmpty(bytes);
  }

  @Override
  public long getSize() {
    return MultipartFileUtils.getSize(bytes);
  }


  @Override
  public InputStream getInputStream() throws IOException {
    return MultipartFileUtils.getInputStream(bytes);
  }

  @Override
  public void transferTo(File dest) throws IOException, IllegalStateException {
    MultipartFileUtils.transferTo(dest);
  }
}
