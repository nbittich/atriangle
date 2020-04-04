#!/usr/bin/env bash
set -e
if [[ ! -d $HOME/.m2/repository/fr/sparna/rdf/xls2rdf/xls2rdf-lib/ ]]
then
  currentDir=$(pwd)
  cd
  rm -rf .m2
  cd /tmp
  rm -rf 2.0.3.zip
  rm -rf xls2rdf-2.0.3
  wget https://github.com/sparna-git/xls2rdf/archive/2.0.3.zip
  unzip 2.0.3.zip
  cd xls2rdf-2.0.3
  mvn clean install -DskipTests
  cd $currentDir
else
  echo "xls2rdf already installed"
fi
