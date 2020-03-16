package tech.artcoded.atriangle.rest.project;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.CheckedFunction;
import tech.artcoded.atriangle.api.CheckedSupplier;
import tech.artcoded.atriangle.api.IdGenerators;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.EventType;
import tech.artcoded.atriangle.api.dto.FileEvent;
import tech.artcoded.atriangle.api.dto.FileEventType;
import tech.artcoded.atriangle.api.dto.KafkaEvent;
import tech.artcoded.atriangle.api.dto.ProjectEvent;
import tech.artcoded.atriangle.api.dto.RestEvent;
import tech.artcoded.atriangle.api.dto.SinkRequest;
import tech.artcoded.atriangle.core.kafka.LoggerAction;
import tech.artcoded.atriangle.core.rest.util.ATriangleByteArrayMultipartFile;
import tech.artcoded.atriangle.core.rest.util.RestUtil;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;
import tech.artcoded.atriangle.feign.clients.shacl.ShaclRestFeignClient;
import tech.artcoded.atriangle.feign.clients.skosplay.SkosPlayRestFeignClient;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
// TODO this class should probably be splitted in different parts / microservices
public class ProjectRestService {

  private static final String DERIVED_FILE_REGEX = "-derived-output-";
  private static final String DERIVED_FILE_SKOS_REGEX = DERIVED_FILE_REGEX + "skos-";

  private final MongoTemplate mongoTemplate;
  private final FileRestFeignClient fileRestFeignClient;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapperWrapper objectMapperWrapper;
  private final ShaclRestFeignClient shaclRestFeignClient;
  private final SkosPlayRestFeignClient skosPlayRestFeignClient;
  private final LoggerAction loggerAction;

  @Value("${spring.kafka.template.default-topic}")
  private String topicProducer;

  @Inject
  public ProjectRestService(MongoTemplate mongoTemplate,
                            FileRestFeignClient fileRestFeignClient,
                            KafkaTemplate<String, String> kafkaTemplate,
                            ObjectMapperWrapper objectMapperWrapper,
                            ShaclRestFeignClient shaclRestFeignClient,
                            SkosPlayRestFeignClient skosPlayRestFeignClient,
                            LoggerAction loggerAction) {
    this.mongoTemplate = mongoTemplate;
    this.fileRestFeignClient = fileRestFeignClient;
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapperWrapper = objectMapperWrapper;
    this.shaclRestFeignClient = shaclRestFeignClient;
    this.skosPlayRestFeignClient = skosPlayRestFeignClient;
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
    return Optional.ofNullable(mongoTemplate.findById(projectId, ProjectEvent.class));
  }

  public List<ProjectEvent> findAll() {
    return mongoTemplate.findAll(ProjectEvent.class);
  }

  public Optional<ProjectEvent> findByName(String name) {
    Query query = new Query().addCriteria(Criteria.where("name").is(name));
    return Optional.ofNullable(mongoTemplate.findOne(query, ProjectEvent.class));
  }

  public void deleteByName(String name) {
    Query query = new Query().addCriteria(Criteria.where("name").is(name));
    mongoTemplate.remove(query);
  }

  public void deleteFile(String projectId, String fileEventId) {
    Optional<ProjectEvent> projectEvent = findById(projectId);
    projectEvent.ifPresent((p) -> {
      Optional<FileEvent> fileEvent = p.getFileEvents().stream().filter(file -> file.getId().equals(fileEventId)).findAny();
      fileEvent.ifPresent(f -> {
        loggerAction.info(p::getId, " file %s removed from project %s", f.getId(), p.getName());
        fileRestFeignClient.delete(f.getId());
        ProjectEvent newProject = p.toBuilder()
                                   .fileEvents(p.getFileEvents()
                                                .stream()
                                                .filter(file -> !file.getId().equals(fileEventId))
                                                .collect(toList()))
                                   .build();
        mongoTemplate.save(newProject);
      });
    });
  }

  public void deleteById(String id) {
    mongoTemplate.remove(id);
  }

  public ResponseEntity<ByteArrayResource> downloadFile(String projectId, String fileEventId) {
    try {
      Optional<ProjectEvent> projectEvent = findById(projectId);
      return projectEvent.flatMap(p -> p.getFileEvents().stream().filter(file -> file.getId().equals(fileEventId)).findFirst())
                         .map(CheckedFunction.toFunction(f -> fileRestFeignClient.download(f.getId())))
                         .orElseGet(ResponseEntity.notFound()::build);
    }
    catch (Exception e) {
      log.error("error", e);
      loggerAction.error(() -> projectId, "an error occured %s", e.getMessage());
    }
    return ResponseEntity.notFound().build();
  }

  public Optional<FileEvent> getFileMetadata(String projectId, String fileEventId) {
    Optional<ProjectEvent> projectEvent = findById(projectId);
    return projectEvent.flatMap(p -> p.getFileEvents().stream().filter(file -> file.getId().equals(fileEventId)).findFirst());
  }

  public Optional<ProjectEvent> addFile(String projectId, MultipartFile file) {
    return addFile(projectId, file, FileEventType.PROJECT_FILE);
  }

  public Optional<ProjectEvent> addFile(String projectId, MultipartFile file, FileEventType fileEventType) {
    return findById(projectId)
      .stream()
      .map(projectEvent -> {
        ResponseEntity<FileEvent> fileEvent = CheckedSupplier.toSupplier(() -> fileRestFeignClient.upload(file, fileEventType))
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

  public ResponseEntity<String> shaclValidation(String projectId, String shapesFileId, String rdfModelFileId) {
    FileEvent shaclFileEvent = getFileMetadata(projectId, shapesFileId).orElseThrow(() -> new RuntimeException("shacl not found in project"));
    FileEvent rdfFileEvent = getFileMetadata(projectId, rdfModelFileId).orElseThrow(() -> new RuntimeException("rdf model not found in project"));
    return shaclRestFeignClient.validate(shaclFileEvent, rdfFileEvent);
  }

  public ResponseEntity<Map<String, String>> skosPing() {
    return skosPlayRestFeignClient.ping();
  }

  @SneakyThrows
  public Optional<ProjectEvent> skosConversion(String projectId,
                                               boolean labelSkosXl,
                                               boolean ignorePostTreatmentsSkos,
                                               FileEvent xlsFileEvent) {

    MultipartFile xlsInput = RestUtil.transformToMultipartFile(xlsFileEvent, () -> downloadFile(projectId, xlsFileEvent.getId())
      .getBody()
      .getByteArray());
    String contentType = "text/turtle";
    ResponseEntity<ByteArrayResource> response = skosPlayRestFeignClient.convertRDF("file", xlsInput,
                                                                                    "fr",
                                                                                    null,
                                                                                    contentType,
                                                                                    labelSkosXl,
                                                                                    false,
                                                                                    false, ignorePostTreatmentsSkos);

    ByteArrayResource body = response.getBody();

    String baseFileName = FilenameUtils.removeExtension(xlsFileEvent.getName());
    String outputFilename = baseFileName + DERIVED_FILE_SKOS_REGEX + IdGenerators.UUID_SUPPLIER.get() + ".ttl";

    MultipartFile rdfOutput = ATriangleByteArrayMultipartFile.builder().contentType(contentType)
                                                             .name(outputFilename)
                                                             .originalFilename(outputFilename)
                                                             .bytes(body.getByteArray())
                                                             .build();

    return this.addFile(projectId, rdfOutput, FileEventType.SKOS_PLAY_CONVERTER_OUTPUT);
  }

  public void sink(SinkRequest sinkRequest) {
    CompletableFuture.runAsync(() -> {
      String projectId = sinkRequest.getProjectId();
      ProjectEvent projectEvent = findById(projectId).orElseThrow();
      String ns = Optional.ofNullable(sinkRequest.getNamespace())
                          .filter(StringUtils::isNotEmpty)
                          .orElseGet(projectEvent::getName);

      RestEvent restEvent = RestEvent.builder()
                                     .namespace(ns)
                                     .elasticIndex(ns)
                                     .sinkToElastic(sinkRequest.isSinkToElastic())
                                     .elasticSettingsJson(getFileMetadata(projectId, sinkRequest.getElasticSettingsFileEventId())
                                                            .orElse(null))
                                     .elasticMappingsJson(getFileMetadata(projectId, sinkRequest.getElasticMappingsFileEventId())
                                                            .orElse(null))
                                     .build();

      KafkaEvent kafkaEvent = KafkaEvent.builder()
                                        .eventType(EventType.RDF_SINK)
                                        .correlationId(projectEvent.getId())
                                        .id(IdGenerators.get())
                                        .shaclModel(getFileMetadata(projectId, sinkRequest.getShaclFileEventId()).orElse(null))
                                        .inputToSink(getFileMetadata(projectId, sinkRequest.getRdfFileEventId()).orElseThrow())
                                        .event(objectMapperWrapper.serialize(restEvent))
                                        .build();

      ProducerRecord<String, String> restRecord = new ProducerRecord<>(topicProducer, kafkaEvent.getId(), objectMapperWrapper.serialize(kafkaEvent));

      kafkaTemplate.send(restRecord);
    });

  }
}
