#!/bin/bash
if [ $# -ne 3 ]; then
	echo "Usage: $0 RmiClientName ClientRMIRegistryIp AgentRMIRegistryIP"
	exit 1
fi

DIR="$(dirname $0)"
java -classpath ./lib/*:./classes -Djava.rmi.server.codebase="file:${DIR}/classes/" -Djava.rmi.server.hostname=$2 jcu.sal.client.stressTest.RmiClientDummyStressTest2 $1 $2 $3

