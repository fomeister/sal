#!/bin/bash
if [ $# -ne 1 ]; then
	echo "Usage: $0 ClientName"
	exit 1
fi


#
# Enables RMI loader debug
#
#DEBUG=-Dsun.rmi.loader.logLevel=VERBOSE


DIR="$(dirname $0)"
java -classpath ./lib/*:./classes -Djava.rmi.server.codebase="file:${DIR}/classes/" ${DEBUG} -Djava.rmi.server.hostname=$2 jcu.sal.client.gui.view.ClientViewImpl $1

