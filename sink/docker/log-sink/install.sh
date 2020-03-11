#!/usr/bin/env bash
rm -rf build

mkdir build

cp ../../log-sink/target/atriangle-log-sink.jar build

docker build -t atriangle/log-sink  . --no-cache
