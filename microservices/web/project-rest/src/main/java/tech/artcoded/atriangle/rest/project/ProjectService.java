package tech.artcoded.atriangle.rest.project;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.artcoded.atriangle.api.dto.FileEvent;
import tech.artcoded.atriangle.api.dto.ProjectEvent;
import tech.artcoded.atriangle.core.kafka.LoggerAction;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {
  private final MongoTemplate mongoTemplate;
  private final LoggerAction loggerAction;

  @Inject
  public ProjectService(MongoTemplate mongoTemplate, LoggerAction loggerAction) {
    this.mongoTemplate = mongoTemplate;
    this.loggerAction = loggerAction;
  }

  @Transactional
  public void deleteById(String id) {
    mongoTemplate.remove(id);
  }

  public Optional<ProjectEvent> findById(String projectId) {
    return Optional.ofNullable(mongoTemplate.findById(projectId, ProjectEvent.class));
  }

  public Optional<ProjectEvent> updateDescription(String projectId, String description) {
    return findById(projectId).stream()
        .map(
            projectEvent -> {
              loggerAction.info(
                  projectEvent::getId, "description of project %s updated", projectEvent.getName());

              return projectEvent
                  .toBuilder()
                  .lastModifiedDate(new Date())
                  .description(description)
                  .build();
            })
        .map(mongoTemplate::save)
        .findFirst();
  }

  @Transactional
  public void deleteByName(String name) {
    Query query = new Query().addCriteria(Criteria.where("name").is(name));
    mongoTemplate.remove(query);
  }

  public List<ProjectEvent> findAll() {
    return mongoTemplate.findAll(ProjectEvent.class);
  }

  public Optional<ProjectEvent> findByName(String name) {
    Query query = new Query().addCriteria(Criteria.where("name").is(name));
    return Optional.ofNullable(mongoTemplate.findOne(query, ProjectEvent.class));
  }

  public ProjectEvent save(ProjectEvent projectEvent) {
    return mongoTemplate.save(projectEvent);
  }

  @Transactional
  public ProjectEvent newProject(String name, String description, FileEvent... fileEvents) {
    String sanitizedName = StringUtils.trimToEmpty(name).replaceAll("[^A-Za-z]+", "").toLowerCase();
    if (sanitizedName.isEmpty()
        || sanitizedName.length() < 7
        || findByName(sanitizedName).isPresent()) {
      throw new RuntimeException(
          String.format(
              "cannot create project %s. name not valid (minimum 7 alphabetic characters) or already exist",
              name));
    }

    ProjectEvent project =
        ProjectEvent.builder()
            .name(sanitizedName)
            .description(description)
            .fileEvents(Arrays.asList(fileEvents))
            .build();
    ProjectEvent save = mongoTemplate.save(project);
    loggerAction.info(save::getId, "new project with name %s created", sanitizedName);
    return save;
  }
}
