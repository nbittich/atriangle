#!/usr/bin/env bash
set -e # fail script on error

releaseVersion=$1
nextVersion=$2-SNAPSHOT

if [[ -z "$releaseVersion" || -z "$nextVersion" ]]
then
  echo "version mut be provided"
  exit -1;
fi

 echo "release" $releaseVersion ", next" $nextVersion


mvn --batch-mode -Dtag=$releaseVersion release:prepare \
                 -DreleaseVersion=$releaseVersion \
                 -DdevelopmentVersion=$nextVersion