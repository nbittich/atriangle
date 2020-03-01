#!/usr/bin/env bash
mvn clean install
docker build -t atriangle/elastic-sink -f docker/elastic-sink/Dockerfile . --no-cache
docker build -t atriangle/rdf-sink -f docker/rdf-sink/Dockerfile . --no-cache
docker build -t atriangle/file-sink -f docker/file-sink/Dockerfile . --no-cache
docker build -t atriangle/event-dispatcher -f docker/event-dispatcher/Dockerfile . --no-cache
docker build -t atriangle/rest -f docker/rest/Dockerfile . --no-cache
cd docker
docker-compose stop
docker-compose rm -y
docker system prune -f
docker volume prune -f
docker network prune -f
docker-compose up -d --force-recreate



