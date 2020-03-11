#!/usr/bin/env bash
rm -rf build

mkdir build

cp ../../upload-rest/target/atriangle-upload-rest.jar build

docker build -t atriangle/upload-rest . --no-cache
