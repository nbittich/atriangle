package tech.artcoded.atriangle.rest.project;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.CheckedSupplier;
import tech.artcoded.atriangle.api.kafka.FileEvent;
import tech.artcoded.atriangle.api.kafka.FileEventType;
import tech.artcoded.atriangle.api.kafka.ProjectEvent;
import tech.artcoded.atriangle.core.kafka.LoggerAction;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
public class ProjectRestService {
  private final MongoTemplate mongoTemplate;
  private final FileRestFeignClient fileRestFeignClient;
  private final LoggerAction loggerAction;

  @Inject
  public ProjectRestService(MongoTemplate mongoTemplate,
                            FileRestFeignClient fileRestFeignClient,
                            LoggerAction loggerAction) {
    this.mongoTemplate = mongoTemplate;
    this.fileRestFeignClient = fileRestFeignClient;
    this.loggerAction = loggerAction;
  }

  public ProjectEvent newProject(String name, FileEvent... fileEvents) {

    if (findByName(name).isPresent()) {
      throw new RuntimeException(String.format("cannot create project %s. already exist", name));
    }

    ProjectEvent project = ProjectEvent.builder().name(name).fileEvents(Arrays.asList(fileEvents)).build();
    ProjectEvent save = mongoTemplate.save(project);
    loggerAction.info(save::getId, "new project with name %s created", name);
    return save;
  }

  public Optional<ProjectEvent> findById(String projectId) {
    return Optional.ofNullable(mongoTemplate.findById(new ObjectId(projectId), ProjectEvent.class));
  }

  public List<ProjectEvent> findAll() {
    return mongoTemplate.findAll(ProjectEvent.class);
  }

  public Optional<ProjectEvent> findByName(String name) {
    Query query = new Query();
    query.addCriteria(Criteria.where("name").is(name));
    return Optional.ofNullable(mongoTemplate.findOne(query, ProjectEvent.class));
  }

  public Optional<ProjectEvent> addFile(String projectId, MultipartFile file) {
    return findById(projectId)
      .stream()
      .map(projectEvent -> {
        ResponseEntity<FileEvent> fileEvent = CheckedSupplier.toSupplier(() -> fileRestFeignClient.upload(file, FileEventType.PROJECT_FILE))
                                                             .get();
        if (!HttpStatus.OK.equals(fileEvent.getStatusCode()) || !fileEvent.hasBody()) {
          throw new RuntimeException("upload failed with status " + fileEvent.getStatusCode());
        }

        loggerAction.info(projectEvent::getId, "new file %s added to project %s", fileEvent.getBody()
                                                                                           .getName(), projectEvent.getName());

        return projectEvent.toBuilder()
                           .fileEvents(Stream.concat(projectEvent.getFileEvents().stream(), Stream.of(fileEvent.getBody()))
                                             .collect(toList()))
                           .build();
      })
      .map(mongoTemplate::save)
      .findFirst();
  }
}
