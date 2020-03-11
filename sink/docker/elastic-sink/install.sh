#!/usr/bin/env bash
rm -rf build

mkdir build
ls -l
cp ../../elastic-sink/target/atriangle-elastic-sink.jar build

docker build -t atriangle/elastic-sink . --no-cache
