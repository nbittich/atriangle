package tech.artcoded.atriangle.rest.project;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.artcoded.atriangle.api.CheckedFunction;
import tech.artcoded.atriangle.api.CheckedSupplier;
import tech.artcoded.atriangle.api.DateHelper;
import tech.artcoded.atriangle.api.ObjectMapperWrapper;
import tech.artcoded.atriangle.api.dto.FileEvent;
import tech.artcoded.atriangle.api.dto.FileEventType;
import tech.artcoded.atriangle.api.dto.ProjectEvent;
import tech.artcoded.atriangle.core.kafka.LoggerAction;
import tech.artcoded.atriangle.feign.clients.file.FileRestFeignClient;
import tech.artcoded.atriangle.feign.clients.shacl.ShaclRestFeignClient;
import tech.artcoded.atriangle.feign.clients.util.FeignMultipartFile;
import tech.artcoded.atriangle.feign.clients.xls2rdf.Xls2RdfRestFeignClient;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
  private final ShaclRestFeignClient shaclRestFeignClient;
  private final Xls2RdfRestFeignClient skosPlayRestFeignClient;
  private final LoggerAction loggerAction;


  @Inject
  public ProjectRestService(MongoTemplate mongoTemplate,
                            FileRestFeignClient fileRestFeignClient,
                            ObjectMapperWrapper objectMapperWrapper,
                            ShaclRestFeignClient shaclRestFeignClient,
                            Xls2RdfRestFeignClient skosPlayRestFeignClient,
                            LoggerAction loggerAction) {
    this.mongoTemplate = mongoTemplate;
    this.fileRestFeignClient = fileRestFeignClient;
    this.shaclRestFeignClient = shaclRestFeignClient;
    this.skosPlayRestFeignClient = skosPlayRestFeignClient;
    this.loggerAction = loggerAction;
  }

  @Transactional
  public ProjectEvent newProject(String name, FileEvent... fileEvents) {

    if (findByName(name).isPresent()) {
      throw new RuntimeException(String.format("cannot create project %s. already exist", name));
    }

    ProjectEvent project = ProjectEvent.builder()
                                       .name(name)
                                       .fileEvents(Arrays.asList(fileEvents))
                                       .build();
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
    Query query = new Query().addCriteria(Criteria.where("name")
                                                  .is(name));
    return Optional.ofNullable(mongoTemplate.findOne(query, ProjectEvent.class));
  }

  @Transactional
  public void deleteByName(String name) {
    Query query = new Query().addCriteria(Criteria.where("name")
                                                  .is(name));
    mongoTemplate.remove(query);
  }

  @Transactional
  public void deleteFile(String projectId, String fileEventId) {
    Optional<ProjectEvent> projectEvent = findById(projectId);
    projectEvent.ifPresent((p) -> {
      Optional<FileEvent> fileEvent = p.getFileEvents()
                                       .stream()
                                       .filter(file -> file.getId()
                                                           .equals(fileEventId))
                                       .findAny();
      fileEvent.ifPresent(f -> {
        loggerAction.info(p::getId, " file %s removed from project %s", f.getId(), p.getName());
        fileRestFeignClient.delete(f.getId());
        ProjectEvent newProject = p.toBuilder()
                                   .fileEvents(p.getFileEvents()
                                                .stream()
                                                .filter(file -> !file.getId()
                                                                     .equals(fileEventId))
                                                .collect(toList()))
                                   .build();
        mongoTemplate.save(newProject);
      });
    });
  }

  @Transactional
  public void deleteById(String id) {
    mongoTemplate.remove(id);
  }

  public ResponseEntity<ByteArrayResource> downloadFile(String projectId, String fileEventId) {
    try {
      Optional<ProjectEvent> projectEvent = findById(projectId);
      return projectEvent.flatMap(p -> p.getFileEvents()
                                        .stream()
                                        .filter(file -> file.getId()
                                                            .equals(fileEventId))
                                        .findFirst())
                         .map(CheckedFunction.toFunction(f -> fileRestFeignClient.download(f.getId(), projectId)))
                         .orElseGet(ResponseEntity.notFound()::build);
    }
    catch (Exception e) {
      log.error("error", e);
      loggerAction.error(() -> projectId, "an error occured %s", e.getMessage());
    }
    return ResponseEntity.notFound()
                         .build();
  }

  public Optional<FileEvent> getFileMetadata(String projectId, String fileEventId) {
    Optional<ProjectEvent> projectEvent = findById(projectId);
    return projectEvent.flatMap(p -> p.getFileEvents()
                                      .stream()
                                      .filter(file -> file.getId()
                                                          .equals(fileEventId))
                                      .findFirst());
  }

  public Optional<ProjectEvent> addFile(String projectId, MultipartFile file) {
    return addFile(projectId, file, FileEventType.PROJECT_FILE);
  }

  @Transactional
  public Optional<ProjectEvent> addFile(String projectId, MultipartFile file, FileEventType fileEventType) {
    return findById(projectId)
      .stream()
      .map(projectEvent -> {
        ResponseEntity<FileEvent> fileEvent = CheckedSupplier.toSupplier(() -> fileRestFeignClient.upload(file, fileEventType, projectId))
                                                             .get();
        if (!HttpStatus.OK.equals(fileEvent.getStatusCode()) || !fileEvent.hasBody()) {
          throw new RuntimeException("upload failed with status " + fileEvent.getStatusCode());
        }

        loggerAction.info(projectEvent::getId, "new file %s added to project %s", fileEvent.getBody()
                                                                                           .getName(), projectEvent.getName());

        return projectEvent.toBuilder()
                           .fileEvents(Stream.concat(projectEvent.getFileEvents()
                                                                 .stream(), Stream.of(fileEvent.getBody()))
                                             .collect(toList()))
                           .build();
      })
      .map(mongoTemplate::save)
      .findFirst();
  }

  public ResponseEntity<String> shaclValidation(String projectId, String shapesFileId, String rdfModelFileId) {
    //todo check belongs to project
    return shaclRestFeignClient.validate(projectId, shapesFileId, rdfModelFileId);
  }

  @SneakyThrows
  public Optional<ProjectEvent> skosConversion(String projectId,
                                               boolean labelSkosXl,
                                               boolean ignorePostTreatmentsSkos,
                                               FileEvent xlsFileEvent) {

    MultipartFile xlsInput = FeignMultipartFile.builder()
                                               .contentType(xlsFileEvent.getContentType())
                                               .name(xlsFileEvent.getName())
                                               .originalFilename(xlsFileEvent.getName())
                                               .bytes(downloadFile(projectId, xlsFileEvent.getId())
                                                        .getBody()
                                                        .getByteArray())
                                               .build();
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
    String outputFilename = baseFileName.split(DERIVED_FILE_REGEX)[0] + DERIVED_FILE_SKOS_REGEX + DateHelper.formatCurrentDateForFilename() + ".ttl";
    MultipartFile rdfOutput = FeignMultipartFile.builder()
                                                .contentType(contentType)
                                                .name(outputFilename)
                                                .originalFilename(outputFilename)
                                                .bytes(body.getByteArray())
                                                .build();

    return this.addFile(projectId, rdfOutput, FileEventType.SKOS_PLAY_CONVERTER_OUTPUT);
  }

}
