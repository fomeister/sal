#!/bin/bash
if [ $# -ne 3 ]; then
	echo "Usage: $0 RmiClientName ClientRMIRegistryIp AgentRMIRegistryIP"
	exit 1
fi

java -classpath ./lib/*:./bin -Djava.rmi.server.codebase=file:/home/gilles/workspace/SAL/bin/ -Djava.rmi.server.hostname=$2 jcu.sal.RmiClient $1 $2 $3

