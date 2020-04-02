#!/usr/bin/env bash
set -e # fail script on error

mvn clean
rm -rf '***.idea'
rm -rf '**/.vscode'
rm -rf '**/.classpath'
rm -rf '**/.factorypath'
rm -rf '**/.settings'
rm -rf '**/.project'
rm -rf '**/*.iml' 

