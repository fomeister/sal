#java -classpath ./lib/log4j-1.2.15.jar:./bin jcu.sal.Agent.SALAgent "src/pc-osdata-sg1.xml" "src/sensors.xml"
java -classpath ./lib/*:./classes:../HAL/lib/*:../HAL/bin:../v4l4j/* -Djava.library.path=../v4l4j:../HAL/lib jcu.sal.StressTest3 "src/platformConfig-empty.xml" "src/sensors-empty-hb.xml"

