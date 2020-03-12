package tech.artcoded.atriangle.rest.project;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.CheckedSupplier;
import tech.artcoded.atriangle.api.kafka.FileEvent;
import tech.artcoded.atriangle.api.kafka.FileEventType;
import tech.artcoded.atriangle.api.kafka.ProjectEvent;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
public class ProjectRestService {
  private final MongoTemplate mongoTemplate;
  private final FileRestFeignClient fileRestFeignClient;

  @Inject
  public ProjectRestService(MongoTemplate mongoTemplate,
                            FileRestFeignClient fileRestFeignClient) {
    this.mongoTemplate = mongoTemplate;
    this.fileRestFeignClient = fileRestFeignClient;
  }

  public ProjectEvent newProject(String name, FileEvent... fileEvents) {
    ProjectEvent project = ProjectEvent.builder().name(name).fileEvents(Arrays.asList(fileEvents)).build();
    return mongoTemplate.save(project);
  }

  public Optional<ProjectEvent> findById(String projectId) {
    return Optional.ofNullable(mongoTemplate.findById(new ObjectId(projectId), ProjectEvent.class));
  }

  public Optional<ProjectEvent> addFile(String projectId, MultipartFile file) {
    return findById(projectId)
      .stream()
      .map(projectEvent -> {
        ResponseEntity<FileEvent> fileEvent = CheckedSupplier.toSupplier(() -> fileRestFeignClient.upload(file, FileEventType.PROJECT_FILE))
                                                             .get();
        if (!HttpStatus.OK.equals(fileEvent.getStatusCode())) {
          throw new RuntimeException("upload failed with status " + fileEvent.getStatusCode());
        }

        return projectEvent.toBuilder()
                           .fileEvents(Stream.concat(projectEvent.getFileEvents().stream(), Stream.of(fileEvent.getBody()))
                                             .collect(toList()))
                           .build();
      })
      .map(mongoTemplate::save)
      .findFirst();
  }
}
