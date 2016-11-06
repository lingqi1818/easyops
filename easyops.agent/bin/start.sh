rm -rf ../target/lib/easyops.config-0.1.jar
java -cp ../conf -Djava.ext.dirs=../target:../target/lib com.dumpcache.easyops.agent.Agent > nohup.log 2>&1 &
