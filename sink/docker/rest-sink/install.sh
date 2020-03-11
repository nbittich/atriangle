#!/usr/bin/env bash
rm -rf build

mkdir build

cp ../../rest-sink/target/atriangle-rest-sink.jar build

docker build -t atriangle/rest-sink . --no-cache
