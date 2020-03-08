package tech.artcoded.atriangle.upload;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.core.database.CrudService;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService implements CrudService<String, FileUpload> {

  private final FileUploadRepository repository;

  @Value("${application.filePath}")
  private String apPath;

  @Inject
  public FileUploadService(FileUploadRepository repository) {
    this.repository = repository;
  }

  @Override
  public FileUploadRepository getRepository() {
    return repository;
  }

  private File getDirectory() {
    File directory = new File(apPath);
    if (!directory.exists() || !directory.isDirectory()) {
      directory.mkdirs();
    }
    return directory;
  }

  @Transactional
  public FileUpload upload(MultipartFile file, FileUploadType uploadType) throws Exception {
    File upload = new File(getDirectory(), UUID.randomUUID()
                                               .toString() + "_" + file.getOriginalFilename());
    file.transferTo(upload);
    FileUpload apUpload = FileUpload.newUpload(file, uploadType, upload.getAbsolutePath());
    return repository.save(apUpload);
  }

  @Transactional
  public FileUpload upload(String contentType, String filename, FileUploadType uploadType,
                           byte[] file) throws IOException {
    File upload = new File(getDirectory(), UUID.randomUUID()
                                               .toString() + '_' + filename);
    FileUtils.writeByteArrayToFile(upload, file);
    FileUpload uploadNew = FileUpload.newUpload(contentType, filename, uploadType, upload.getAbsolutePath());
    uploadNew.setSize(file.length);
    return repository.save(uploadNew);
  }

  public Page<FileUpload> findAllByCreatedBy(String author, Pageable pageable) {
    return repository.findAllByCreatedBy(author, pageable);
  }

  public Page<FileUpload> findAllByUploadType(FileUploadType uploadType, Pageable pageable) {
    return repository.findAllByUploadType(uploadType, pageable);
  }

  public List<FileUpload> findAllByUploadType(FileUploadType uploadType) {
    return repository.findAllByUploadType(uploadType);
  }

  public File uploadToFile(FileUpload upload) {
    return new File(upload.getPathToFile());
  }

  public byte[] uploadToByteArray(FileUpload upload) {
    try {
      return FileUtils.readFileToByteArray(uploadToFile(upload));
    }
    catch (IOException e) {
      return null;
    }
  }

  @Override
  public void delete(FileUpload upload) {
    repository.delete(upload);
  }

  @SneakyThrows
  public void deleteOnDisk(FileUpload upload) {
    FileUtils.forceDelete(new File(upload.getPathToFile()));
    repository.delete(upload);
  }

}
