#!/usr/bin/env bash
set -e # fail script on error

find . -name '*.idea' -exec rm -r {} \;
find . -name '*.classpath' -exec rm -r {} \;
find . -name '*.factorypath' -exec rm -r {} \;
find . -name '*.settings' -exec rm -r {} \;
find . -name '*.project' -exec rm -r {} \;
find . -name '*.iml' -exec rm -r {} \;

