#!/usr/bin/env bash
set -e # fail script on error

releaseVersion=$1
nextVersion=$2-SNAPSHOT

if [[ -z "$1" || -z "$2" ]]
then
  echo "version mut be provided"
  exit -1;
fi

echo "install application"
bash install.sh

echo "wait till app is running (90s)"
sleep 90

echo "running test"
mvn clean install -DtestSuite=integration

echo "release" $releaseVersion ", next" $nextVersion
mvn release:clean

mvn --batch-mode -Dtag=$releaseVersion release:prepare \
                 -DreleaseVersion=$releaseVersion \
                 -DdevelopmentVersion=$nextVersion

mvn release:clean
git pull

rm -f RELEASE_NOTE.md
touch RELEASE_NOTE.md
echo "# Release note\n\n## $2" >> RELEASE_NOTE.md
git add RELEASE_NOTE.md
git commit -m "next release note"
git push
