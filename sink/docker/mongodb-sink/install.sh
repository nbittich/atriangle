#!/usr/bin/env bash
rm -rf build

mkdir build

cp ../..//mongodb-sink/target/atriangle-mongodb-sink.jar build

docker build -t atriangle/mongodb-sink . --no-cache
