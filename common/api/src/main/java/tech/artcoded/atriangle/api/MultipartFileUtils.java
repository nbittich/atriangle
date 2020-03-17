package tech.artcoded.atriangle.api;

import org.apache.commons.lang3.NotImplementedException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface MultipartFileUtils {

  static boolean isEmpty(byte[] bytes) {
    return bytes == null || bytes.length == 0;
  }

  static long getSize(byte[] bytes) {
    return bytes.length;
  }

  static InputStream getInputStream(byte[] bytes) throws IOException {
    return new ByteArrayInputStream(bytes);
  }

  static void transferTo(File dest) throws IOException, IllegalStateException {
    throw new NotImplementedException("not supported yet");
  }
}
