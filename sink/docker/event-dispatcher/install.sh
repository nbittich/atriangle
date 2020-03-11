#!/usr/bin/env bash
rm -rf build

mkdir build

cp ../../event-dispatcher/target/atriangle-event-dispatcher.jar build

docker build -t atriangle/event-dispatcher . --no-cache
