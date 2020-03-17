# WIP A Triangle

  A microservice architecture platform designed to handle RDF data. 

 ![Screenshot](./docs/atriangle.png?raw=true)

  ## Architecture Whatever Schema

  ![Screenshot](./docs/architecture.png?raw=true)

  ## Minimum requirements
  -  Java 11
  -  Maven
  -  Docker
  -  Docker-compose
  -  Tested on a unix system with 8Gb of ram & 6 cores assigned to the docker vm

  ## Quick start guide

  - clone the project
  - run sh install.sh
  - go to http://localhost:8088/api/project/swagger-ui.html to see the api doc

  ## Example #1 Skos conversion & ingestion to triplestore + mongodb (NO SHACL Validation)

  - create project

  - add a xlsx skosplay compatible file to the project (e.g docs/examples/excel2skos-exemple-1.xlsx)

  - run skos conversion

  - sink

  - check result in mongodb

  - check result in blazegraph

  ## About
  work in progress...

  ![Screenshot](./docs/starhopper.gif?raw=true?style=center)
