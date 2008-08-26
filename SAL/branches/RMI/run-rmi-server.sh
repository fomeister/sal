#!/bin/bash

if [ $# -ne 1 ]; then
	echo "Usage: $0 <IP address of the agent's RMI registry>"
	exit 1
fi

PLATFORM_CONFIG=conf/platformConfig-empty.xml
SENSORS_CONFIG=conf/sensors-empty.xml
SAL_DIR=$(dirname $0)
SAL_BIN=${SAL_DIR}/classes
SAL_LIB=${SAL_DIR}/lib

rm ${PLATFORM_CONFIG}
rm ${SENSORS_CONFIG}

#
# Enables RMI loader debug
#
#DEBUG=-Dsun.rmi.loader.logLevel=VERBOSE


java -classpath ${SAL_BIN}:${SAL_LIB}/*:../HAL/lib/*:../HAL/bin:../v4l4j/* -Djava.library.path=../v4l4j:../HAL/lib -Djava.rmi.server.codebase="file:${SAL_BIN}/ file:${SAL_LIB}/log4j-1.2.15.jar" ${DEBUG} -Djava.rmi.server.hostname=$1 jcu.sal.agent.RMIAgentImpl $1 ${PLATFORM_CONFIG} ${SENSORS_CONFIG}

