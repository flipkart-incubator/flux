#!/bin/bash
[ $# -lt 1 ] && echo "Usage `basename $0` <Example FQN>" && exit 1
EXAMPLE_FQN=$1
echo "Copying dependencies, this may take a while"
mvn -q dependency:copy-dependencies -DincludeScope=runtime -DskipTests
echo "Running $EXAMPLE_FQN for you "
java -cp "target/examples-1.0-SNAPSHOT.jar:target/dependency/*" $EXAMPLE_FQN
