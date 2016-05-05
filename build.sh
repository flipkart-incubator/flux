#!/bin/bash
set -x
set -e
echo "Usage: ./build.sh [noTests]"

NO_TESTS_FLAG=$1
echo "Building.."
mvn -q clean install -DskipTests
echo "Checking if we should run tests.."
if [ "$NO_TESTS_FLAG" != "noTests" ]; then
  ./setup_db.sh
  mvn clean integration-test
fi
