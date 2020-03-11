#!/usr/bin/env bash

echo "deleting old build_package"
mvn clean install

echo "running sink install"
cd sink
sh install.sh
cd ..

echo "running webservice install"
cd webservice
sh install.sh
cd ..

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
echo "docker logs -f docker_atrianglemongodbsink_1"
echo "docker logs -f docker_atrianglerdfsink_1"
echo "docker logs -f docker_atriangleeventdispatcher_1"
echo "docker logs -f docker_atrianglerest_1"
echo "docker logs -f docker_atriangleelasticsink_1"
echo "docker logs -f docker_atriangleuploadrest_1"



