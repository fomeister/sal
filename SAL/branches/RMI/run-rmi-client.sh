#!/bin/bash
if [ $# -ne 3 ]; then
	echo "Usage: $0 RmiClientName ClientRMIRegistryIp AgentRMIRegistryIP"
	exit 1
fi


#
# Enables RMI loader debug
#
#DEBUG=-Dsun.rmi.loader.logLevel=VERBOSE


DIR="$(dirname $0)"
java -classpath ./lib/*:./classes -Djava.rmi.server.codebase="file:${DIR}/classes/" ${DEBUG} -Djava.rmi.server.hostname=$2 jcu.sal.client.RmiClient $1 $2 $3

