#!/bin/bash

PLATFORM_CONFIG=conf/platformConfig-empty.xml
SENSORS_CONFIG=conf/sensorsConfig-empty.xml

rm ${PLATFORM_CONFIG}
rm ${SENSORS_CONFIG}


SAL_DIR=$(dirname $0)
SAL_BIN=${SAL_DIR}/classes
SAL_LIB=${SAL_DIR}/lib


java -classpath ${SAL_BIN}:${SAL_LIB}/*:../HAL/lib/*:../HAL/bin:../v4l4j/* -Djava.library.path=../v4l4j:../HAL/lib $@ jcu.sal.client.SALClient ${PLATFORM_CONFIG} ${SENSORS_CONFIG}

