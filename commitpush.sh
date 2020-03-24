#!/usr/bin/env bash
set -e # fail script on error

# Script to commit & push
# It also lint the proiect and build it before commit

if [[ -z "$1" ]]
then
  echo "commit message mut be provided"
  exit -1;
fi

rm -f _CLOC.*

mvn clean

mvn editorconfig:check
mvn editorconfig:format

cloc  common core microservices --out=_CLOC.txt

mvn clean install

git add .
git commit -m "$1"
git push

