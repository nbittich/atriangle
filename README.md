# WIP A Triangle

![Screenshot](atriangle.png?raw=true)

 ## Requirements
   - Docker
 ## Features
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
