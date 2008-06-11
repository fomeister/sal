#java -classpath ./lib/log4j-1.2.15.jar:./bin jcu.sal.Agent.SALAgent "src/pc-osdata-sg1.xml" "src/sensors.xml"
java -classpath ./lib/*:./bin:../HAL/lib/*:../HAL/bin:../v4l4j/* -Djava.library.path=../v4l4j:../HAL/lib jcu.sal.StressTest3 "src/platformConfig-owfs.xml" "src/sensors-owfs-hb.xml"

