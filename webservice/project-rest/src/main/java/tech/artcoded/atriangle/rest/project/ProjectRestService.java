package tech.artcoded.atriangle.rest.project;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class ProjectRestService {
  private final MongoTemplate mongoTemplate;

  @Inject
  public ProjectRestService(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

}
