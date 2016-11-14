#!/bin/bash
[ $# -lt 1 ] && echo "Usage `basename $0`<SFS Shadow Jar Path> <SFS Flux File Path> <debug> <debug_port>" && exit 1

SHADOW_JAR=$1
CONFIG_FILE=$2
DEBUG=$3
DEBUG_PORT=$4

if [[ $# -ge 2 && "debug" == ${DEBUG} ]]; then
    [ $# -lt 3 ] && echo "Debug port not found. Usage `basename $0` <Example FQN> <debug> <debug_port>" && exit 1
fi

bash copy_sfs.sh $@

JAVA_OPTS="-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=18022 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"

if [[ $# -ge 3 && "debug" == ${DEBUG} ]]; then
    echo "Starting flux runtime in debug mode. Debug port: $DEBUG_PORT"
    java ${JAVA_OPTS} -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=${DEBUG_PORT},suspend=y -cp "target/dependency/*" "com.flipkart.flux.initializer.FluxInitializer"
else
    echo "Starting flux runtime"
    java ${JAVA_OPTS} -cp "target/dependency/*" "com.flipkart.flux.initializer.FluxInitializer"
fi
