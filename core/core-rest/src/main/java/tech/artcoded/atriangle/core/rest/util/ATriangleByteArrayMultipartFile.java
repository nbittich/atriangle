package tech.artcoded.atriangle.core.rest.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ATriangleByteArrayMultipartFile implements MultipartFile {
  private String name;
  private String originalFilename;
  private String contentType;
  private byte[] bytes;

  @Override
  public boolean isEmpty() {
    return bytes == null || bytes.length == 0;
  }

  @Override
  public long getSize() {
    return bytes.length;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream(this.getBytes());
  }

  @Override
  public void transferTo(File dest) throws IOException, IllegalStateException {
    throw new NotImplementedException("not supported yet");
  }
}
