#!/usr/bin/env bash
set -e # fail script on error


docker-compose -f docker/docker-compose-local.yml stop
docker-compose -f docker/docker-compose-local.yml stop

docker-compose -f docker/docker-compose.yml stop
docker-compose -f docker/docker-compose.yml rm --force

docker system prune -f
docker volume prune -f
docker network prune -f

mvn editorconfig:format
mvn clean install -Ddocker

# you must uncomment the next row in case of weird behavior
docker-compose -f docker/docker-compose.yml build  --no-cache
docker-compose -f docker/docker-compose.yml up -d --force-recreate --remove-orphans

echo "ready to use! Containers will restart until they are up, but check the containers using: "
echo "docker ps"

echo "docker logs -f docker_atrianglelogsink_1"
echo "docker logs -f docker_atrianglemongodbsink_1"
echo "docker logs -f docker_atrianglerdfsink_1"
echo "docker logs -f docker_atriangleeventdispatcher_1"

echo "docker logs -f docker_atrianglerestgateway_1"
echo "docker logs -f docker_atriangleelasticsink_1"
echo "docker logs -f docker_atriangleuploadrest_1"
echo "docker logs -f docker_atriangleprojectrest_1"
echo "docker logs -f docker_atriangleshaclrest_1"

echo "Portainer: http://localhost:9000"
echo "App: http://localhost:8088/api/project/swagger-ui.html"




