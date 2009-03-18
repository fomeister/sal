#!/bin/bash
if [ $# -ne 3 ]; then
	echo "Usage: $0 RmiClientName AgentRMIRegistryIp ClientRMIRegistryIP"
	exit 1
fi


#
# Enables RMI loader debug
#
#DEBUG="-Dsun.rmi.loader.logLevel=VERBOSE -Djava.rmi.server.logCalls=true"


DIR="$(dirname $0)"
#java -classpath jar-common.jar -Djava.rmi.server.codebase="file:${DIR}/jar-client.jar" ${DEBUG} -Djava.rmi.server.hostname=$2 jcu.sal.client.RmiClient $1 $2 $3

java -classpath SAL-common.jar:SAL-clients.jar -Djava.rmi.server.codebase="file:${DIR}/SAL-common.jar" ${DEBUG} -Djava.rmi.server.hostname=$3 jcu.sal.client.RmiClient $1 $2 $3

