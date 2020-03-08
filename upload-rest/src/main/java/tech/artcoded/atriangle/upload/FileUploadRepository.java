package tech.artcoded.atriangle.upload;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, String> {
  Page<FileUpload> findAllByCreatedBy(String author, Pageable pageable);

  Page<FileUpload> findAllByUploadType(FileUploadType uploadType, Pageable pageable);

  List<FileUpload> findAllByUploadType(FileUploadType uploadType);

}
