#!/bin/bash
[ $# -lt 1 ] && echo "Usage `basename $0` <Example FQN>" && exit 1
EXAMPLE_FQN=$1
DEPLOYMENT_UNIT_PATH=/tmp/workflows
DEPLOYMENT_UNIT_NAME=wf1

echo "Building flux modules..."
cd ../
mvn -q clean install -DskipTests
cd examples/

echo "Copying dependencies, this may take a while"
mvn -q dependency:copy-dependencies -DincludeScope=runtime -DskipTests

echo "Creating deployment unit structure"
mkdir -p $DEPLOYMENT_UNIT_PATH/$DEPLOYMENT_UNIT_NAME/main
mkdir -p $DEPLOYMENT_UNIT_PATH/$DEPLOYMENT_UNIT_NAME/lib

echo "Copying jars to deployment unit"
cp target/examples-1.0-SNAPSHOT.jar $DEPLOYMENT_UNIT_PATH/$DEPLOYMENT_UNIT_NAME/main
cp target/dependency/* $DEPLOYMENT_UNIT_PATH/$DEPLOYMENT_UNIT_NAME/lib
cp src/main/resources/flux_config.yml $DEPLOYMENT_UNIT_PATH/$DEPLOYMENT_UNIT_NAME/

echo "Starting flux runtime"
java -cp "target/dependency/*" "com.flipkart.flux.initializer.FluxInitializer" &

sleep 15

echo "Running $EXAMPLE_FQN for you "
#The below code prints the lines in green color
echo "\033[33;32m $(java -cp 'target/examples-1.0-SNAPSHOT.jar:target/dependency/*' $EXAMPLE_FQN)"
#Reset the color
echo "\033[33;0m"

#sleep for 10 seconds before killing flux process
#sleep 10
#kill -9 $(lsof -t -i :2551)
