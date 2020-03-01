# WIP A Triangle

![Screenshot](atriangle.png?raw=true)

 ## How it works
 - A rest endpoint is exposed to save rdf files.
 - The rest endpoint validates the model with the shacl validation file if it was provided.
 - If the model is valid, it is transformed to a Kafka Event (json-ld) and redirect to a dispatcher topic (the event-dispatcher).
 - The event-dispatcher topic extracts, transforms the event and redirect it to the topic of the workers responsible to consume it (e.g elastic-sink,rdf-sink).
 - The Workers (ElasticSink,FileSkink,RdfSink) consumes the message & persist the Jena 

 ## Advantages
   - The event-dispatcher topic is the source of truth. 
   - It can be consumed from start to restore/sync virtuoso/elastic
   - The sub-topics (worker topics) can be regularly deleted
   - Micro service architecture combined with a Message Broker  makes it easier to maintain and develop new functionalities
 
 ## Requirements
   - Docker
 ## Features
   - Serverless Architecture (Todo)
   - Micro service architecture using Spring Boot, Docker & kafka
   - No data losses
   - Dynamic Shacl Validation
   - Dynamic Elastic index settings & mappings (to fix nested & field exclusion)
   - RDF are saved as Kafka events that can be re-run in case of failure 
   - RDF are stored in ElasticSearch as JSON-LD
   - RDF are stored in Virtuoso
   - Swagger
 ## Quick start
  - clone the repository
  - run sh install.sh
  - wait until all container are properly started
  - Link to Swagger: http://localhost:8088/swagger-ui.html
  - Link to Sparql Proxy: http://localhost:8088/proxy/virtuoso
  - Link to Elasticsearch Proxy: http://localhost:8088/proxy/elasticsearch

  1. Send an rdf request with swagger:

  ![Screenshot](swagger.png?raw=true)
  
  2. Check the result in virtuoso

       ```
            select * from <http://artcoded.tech/person/graph> where {?s ?p ?o}
       ```

  ![Screenshot](virtuoso.png?raw=true)   
