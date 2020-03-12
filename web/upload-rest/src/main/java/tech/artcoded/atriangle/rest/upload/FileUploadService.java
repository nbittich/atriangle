package tech.artcoded.atriangle.rest.upload;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.kafka.FileEvent;
import tech.artcoded.atriangle.api.kafka.FileEventType;
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
  public FileEvent upload(MultipartFile file, FileEventType uploadType) throws Exception {
    File upload = new File(getDirectory(), UUID.randomUUID()
                                               .toString() + "_" + FilenameUtils.normalize(file.getOriginalFilename()));
    FileUtils.writeByteArrayToFile(upload, file.getBytes());
    FileUpload apUpload = FileUpload.newUpload(file, uploadType, upload.getAbsolutePath());
    return FileUpload.transform(repository.save(apUpload));
  }

  @Transactional
  public FileUpload upload(String contentType, String filename, FileEventType uploadType,
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

  public Page<FileUpload> findAllByUploadType(FileEventType uploadType, Pageable pageable) {
    return repository.findAllByUploadType(uploadType, pageable);
  }

  public List<FileUpload> findAllByUploadType(FileEventType uploadType) {
    return repository.findAllByUploadType(uploadType);
  }

  public File uploadToFile(FileEvent upload) {
    return new File(upload.getPathToFile());
  }

  public byte[] uploadToByteArray(FileEvent upload) {
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
