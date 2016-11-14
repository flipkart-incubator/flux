#!/bin/bash
[ $# -lt 1 ] && echo "Usage `basename $0`<SFS Shadow Jar Path> <SFS Flux File Path> <debug> <debug_port>" && exit 1


echo "Building flux modules..."
cd ../
mvn -q clean install -DskipTests
cd examples/

echo "Copying dependencies, this may take a while"
mvn -q dependency:copy-dependencies -DincludeScope=runtime -DskipTests

bash run_sfs.sh $@
