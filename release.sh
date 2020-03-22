#!/usr/bin/env bash
set -e # fail script on error

if [[ -z "$1" || -z "$2" ]]
then
  echo "version mut be provided"
  exit -1;
fi
 echo "release" $1 ", next" $2

releaseVersion = $1
nextVersion = $2-SNAPSHOT

mvn --batch-mode -Dtag=$releaseVersion release:prepare \
                 -DreleaseVersion=$releaseVersion \
                 -DdevelopmentVersion=$nextVersion