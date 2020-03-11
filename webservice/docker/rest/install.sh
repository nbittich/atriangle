#!/usr/bin/env bash
rm -rf build

mkdir build

cp ../../rest/target/atriangle-rest.jar build

docker build -t atriangle/rest . --no-cache
