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
java -classpath ./lib/*:./clients-classes:./classes -Djava.rmi.server.codebase="file:${DIR}/clients-classes/" ${DEBUG} jcu.sal.client.gui.view.ClientViewImpl $1

