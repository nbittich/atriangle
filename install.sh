#!/usr/bin/env bash

docker-compose -f docker/docker-compose.yml stop
docker-compose -f docker/docker-compose.yml rm -y
docker system prune -f
docker volume prune -f
docker network prune -f

mvn clean install -Ddocker

docker-compose -f docker/docker-compose.yml up -d --force-recreate

echo "ready to use! Containers will restart until they are up, but check the containers using: "

echo "docker ps"

echo "docker logs -f docker_atrianglerestsink_1"
echo "docker logs -f docker_atrianglelogsink_1"
echo "docker logs -f docker_atrianglemongodbsink_1"
echo "docker logs -f docker_atrianglerdfsink_1"
echo "docker logs -f docker_atriangleeventdispatcher_1"

echo "docker logs -f docker_atrianglerest_1"
echo "docker logs -f docker_atriangleelasticsink_1"
echo "docker logs -f docker_atriangleuploadrest_1"
echo "docker logs -f docker_atriangleprojectrest_1"




