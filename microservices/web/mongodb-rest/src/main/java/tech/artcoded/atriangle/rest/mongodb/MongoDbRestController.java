package tech.artcoded.atriangle.rest.mongodb;


import com.mongodb.BasicDBObject;
import com.mongodb.client.result.DeleteResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.boot.info.BuildProperties;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import tech.artcoded.atriangle.api.RawJsonWrappedResponse;
import tech.artcoded.atriangle.core.rest.annotation.CrossOriginRestController;
import tech.artcoded.atriangle.core.rest.controller.BuildInfoControllerTrait;
import tech.artcoded.atriangle.core.rest.controller.PingControllerTrait;
import tech.artcoded.atriangle.feign.clients.mongodb.MongoDbRestFeignClient;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    if (mongoTemplate.collectionExists(collectionName)) {
      return ResponseEntity.badRequest()
                           .body("collection already exists");
    }
    mongoTemplate.createCollection(collectionName);
    return ResponseEntity.ok("collection created");
  }

  @Override
  public ResponseEntity<Set<String>> listCollections() {
    return ResponseEntity.ok(mongoTemplate.getCollectionNames());
  }

  @Override
  public ResponseEntity<String> deleteCollection(String collectionName) {
    if (!mongoTemplate.collectionExists(collectionName)) {
      return ResponseEntity.badRequest()
                           .body("collection does not exist");
    }
    mongoTemplate.dropCollection(collectionName);
    return ResponseEntity.ok("collection deleted");
  }

  @Override
  public ResponseEntity<String> delete(String collectionName, String id) {
    DeleteResult deleteResult = mongoTemplate.remove(Query.query(Criteria.where("_id")
                                                                         .is(new ObjectId(id))), collectionName);
    log.info("object with id {} deleted. acknowledge {}", id, deleteResult.wasAcknowledged());
    return ResponseEntity.ok(String.format("%s object has been deleted", deleteResult.getDeletedCount()));
  }

  @Override
  public ResponseEntity<RawJsonWrappedResponse> save(String collectionName, String objectToSave) {
    BasicDBObject dbObject = BasicDBObject.parse(objectToSave);
    BasicDBObject savedObject = mongoTemplate.save(dbObject, collectionName);
    return ResponseEntity.ok(new RawJsonWrappedResponse(savedObject.toJson()));
  }

  @Override
  public ResponseEntity<List<RawJsonWrappedResponse>> query(String collectionName, String query) {
    BasicQuery basicQuery = new BasicQuery(query);
    return ResponseEntity.ok(mongoTemplate.find(basicQuery, BasicDBObject.class, collectionName).stream()
                                          .map(BasicDBObject::toJson)
                                          .map(RawJsonWrappedResponse::new)
    .collect(Collectors.toUnmodifiableList()));
  }

  @Override
  public ResponseEntity<Set<RawJsonWrappedResponse>> findAll(String collectionName) {
    List<BasicDBObject> list = mongoTemplate.findAll(BasicDBObject.class, collectionName);
    return ResponseEntity.ok(list.stream()
                                 .map(BasicDBObject::toJson)
                                 .map(RawJsonWrappedResponse::new)
                                 .collect(Collectors.toSet()));
  }

  @Override
  public ResponseEntity<RawJsonWrappedResponse> findById(String collectionName, String id) {
    BasicDBObject object = mongoTemplate.findOne(Query.query(Criteria.where("_id")
                                                                     .is(new ObjectId(id))), BasicDBObject.class, collectionName);
    return Optional.ofNullable(object)
                   .map(BasicDBObject::toJson)
                   .map(RawJsonWrappedResponse::new)
                   .map(ResponseEntity::ok)
                   .orElseGet(ResponseEntity.notFound()::build);
  }
}
