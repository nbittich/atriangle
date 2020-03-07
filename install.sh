#!/usr/bin/env bash
mvn clean install
docker build -t atriangle/elastic-sink -f docker/elastic-sink/Dockerfile . --no-cache
docker build -t atriangle/rdf-sink -f docker/rdf-sink/Dockerfile . --no-cache
docker build -t atriangle/event-dispatcher -f docker/event-dispatcher/Dockerfile . --no-cache
docker build -t atriangle/rest-sink -f docker/rest-sink/Dockerfile . --no-cache
docker build -t atriangle/rest -f docker/rest/Dockerfile . --no-cache
cd docker
docker-compose stop
docker-compose rm -f
docker system prune -f
docker volume prune -f
docker network prune -f
docker-compose up -d --force-recreate


echo "ready to use! Containers will restart until they are up, but check the containers using: "
echo "docker ps"
echo "docker logs -f docker_atrianglerestsink_1"
echo "docker logs -f docker_atrianglelogsink_1"
echo "docker logs -f docker_atrianglerdfsink_1"
echo "docker logs -f docker_atriangleeventdispatcher_1"
echo "docker logs -f docker_atrianglerest_1"
echo "docker logs -f docker_atriangleelasticsink_1"



