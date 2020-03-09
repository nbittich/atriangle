package tech.artcoded.atriangle.rest.project;

import com.mongodb.BasicDBObject;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import tech.artcoded.atriangle.api.IdGenerators;
import tech.artcoded.atriangle.api.ModelConverter;

import javax.inject.Inject;

@Service
public class ProjectRestService implements CommandLineRunner {
  private final MongoTemplate mongoTemplate;

  @Inject
  public ProjectRestService(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public void run(String... args) throws Exception {
    Model model = ModelFactory.createDefaultModel();
    Resource resource = model.createResource("http://artcoded.tech/" + IdGenerators.UUID_SUPPLIER.get());
    Property firstname = ResourceFactory.createProperty("http://artcoded.tech#", "firstname");
    resource.addProperty(firstname, "Bittich");
    BasicDBObject objectToSave = BasicDBObject.parse(ModelConverter.modelToLang(model, Lang.JSONLD));
    mongoTemplate.save(objectToSave, "tests");
  }
}
