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
  
  3. Check the result in elasticsearch 
     * Check index created: http://localhost:8088/proxy/elasticsearch/persons
     * Check rdf indexed: http://localhost:8088/proxy/elasticsearch/persons/_search
```json
     {
        "persons":{
            "aliases":{
            },
            "mappings":{
            },
            "settings":{
                "index":{
                    "creation_date":"1583084835376",
                    "number_of_shards":"1",
                    "number_of_replicas":"1",
                    "uuid":"I4SRjwFtSpmhXTKAMytjOg",
                    "version":{
                        "created":"7060099"
                    },
                    "provided_name":"persons"
              }
            }
        }
     }
```
```json
{
    "took":112,
    "timed_out":false,
    "_shards":{
        "total":1,
        "successful":1,
        "skipped":0,
        "failed":0
    },
    "hits":{
        "total":{
            "value":1,
            "relation":"eq"
        },
        "max_score":1,
        "hits":[
            {
                "_index":"persons",
                "_type":"_doc",
                "_id":"89845a3a-2d82-42f4-b40c-b438fd72de4e",
                "_score":1,
                "_source":{
                    "@id":"http://artcoded.tech/person",
                    "artist":"Nordine Bittich",
                    "company":"Artcoded",
                    "country":"BELGIUM",
                    "year":"1988",
                    "@context":{
                        "year":{
                            "@id":"http://artcoded.tech#year"
                        },
                        "company":{
                            "@id":"http://artcoded.tech#company"
                        },
                        "country":{
                            "@id":"http://artcoded.tech#country"
                        },
                        "artist":{
                            "@id":"http://artcoded.tech#artist"
                        },
                        "rdf":"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
                        "artcoded":"http://artcoded.tech#"
                    }
                }
            }
        ]
    }
}
```
## Helper commands
   ```
   docker ps
   docker logs -f docker_atrianglefilesink_1
   docker logs -f docker_atrianglerdfsink_1
   docker logs -f docker_atriangleeventdispatcher_1
   docker logs -f docker_atrianglerest_1
   docker logs -f docker_atriangleelasticsink_1
   ```
