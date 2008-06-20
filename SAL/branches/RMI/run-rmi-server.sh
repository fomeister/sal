#!/bin/bash

if [ $# -ne 1 ]; then
	echo "Usage: $0 <IP address of the agent's RMI registry>"
	exit 1
fi

PLATFORM_CONFIG=conf/platformConfig-empty.xml
SENSORS_CONFIG=conf/sensors-empty.xml
SAL_DIR=$(dirname $0)

rm ${PLATFORM_CONFIG}
rm ${SENSORS_CONFIG}

#
# Enables RMI loader debug
#
#DEBUG=-Dsun.rmi.loader.logLevel=VERBOSE


java -classpath ./lib/*:./bin:../HAL/lib/*:../HAL/bin:../v4l4j/* -Djava.library.path=../v4l4j:../HAL/lib -Djava.rmi.server.codebase="file:${SAL_DIR}/bin/ file:${SAL_DIR}/lib/log4j-1.2.15.jar" ${DEBUG} -Djava.rmi.server.hostname=$1 jcu.sal.agent.RMIAgentImpl $1 ${PLATFORM_CONFIG} ${SENSORS_CONFIG}

