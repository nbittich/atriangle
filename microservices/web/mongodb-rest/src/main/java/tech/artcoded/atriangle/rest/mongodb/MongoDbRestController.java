package tech.artcoded.atriangle.rest.mongodb;


import com.mongodb.client.MongoCollection;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.info.BuildProperties;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.controller.BuildInfoControllerTrait;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.feign.clients.mongodb.MongoDbRestFeignClient;

import javax.inject.Inject;
import java.util.Set;

@CrossOriginRestController
@Slf4j
public class MongoDbRestController implements PingControllerTrait,
  BuildInfoControllerTrait, MongoDbRestFeignClient {
  @Getter
  private final BuildProperties buildProperties;
  private final MongoTemplate mongoTemplate;

  @Inject
  public MongoDbRestController(BuildProperties buildProperties,
                               MongoTemplate mongoTemplate) {
    this.buildProperties = buildProperties;
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public ResponseEntity<String> createCollection(String collectionName) {
    if (mongoTemplate.collectionExists(collectionName)){
      return ResponseEntity.badRequest().body("collection already exists");
    }
    mongoTemplate.createCollection(collectionName);
    return ResponseEntity.ok("collection created");
  }

  @Override
  public ResponseEntity<Set<String>> listCollections() {
    return ResponseEntity.ok(mongoTemplate.getCollectionNames());
  }
}
