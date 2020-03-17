#!/usr/bin/env bash
set -e # fail script on error

docker-compose -f docker/docker-compose.yml stop
docker-compose -f docker/docker-compose.yml rm --force

docker system prune -f
docker volume prune -f
docker network prune -f

docker-compose -f docker/docker-compose-local.yml up -d --force-recreate --remove-orphans

echo "ready to use!"





