#!/usr/bin/env bash
rm -rf build

mkdir .build

cp ../../rest-sink/target/atriangle-rdf-sink.jar build

docker build -t atriangle/rdf-sink . --no-cache
