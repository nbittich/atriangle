#!/usr/bin/env bash
cd docker/elastic-sink/
sh install.sh
cd ../log-sink/
sh install.sh
cd ../mongodb-sink/
sh install.sh
cd ../rdf-sink
sh /install.sh
cd ../rest-sink/
sh install.sh
cd ../event-dispatcher
sh install.sh
cd ../..
