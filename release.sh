#!/usr/bin/env bash
set -e # fail script on error

releaseVersion=$1
nextVersion=$2-SNAPSHOT

if [[ -z "$1" || -z "$2" ]]
then
  echo "version mut be provided"
  exit -1;
fi

mvn editorconfig:format
git add .
git commit -m "Formatting before release"
git push

echo "release" $releaseVersion ", next" $nextVersion
mvn release:clean

mvn --batch-mode -Dtag=$releaseVersion release:prepare \
                 -DreleaseVersion=$releaseVersion \
                 -DdevelopmentVersion=$nextVersion

mvn release:clean
git pull
