#!/usr/bin/env bash

JETTY_INSTANCES=0

JAVA_OPTS=" -server -Xms4096m -Xmx4096m  -Xss256K -XX:PermSize=512M -XX:MaxPermSize=512M  -XX:+UseConcMarkSweepGC -XX:+DisableExplicitGC  -XX:+UseParNewGC -XX:+CMSClassUnloadingEnabled -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:+UseCompressedOops -XX:CMSInitiatingOccupancyFraction=70 -XX:+HeapDumpOnOutOfMemoryError -XX:SurvivorRatio=8 "

JAVA_OPTS=" -Dnet.spy.log.LoggerImpl=net.spy.memcached.compat.log.Log4JLogger ${JAVA_OPTS}"

get_jetty_instance(){
JETTY_INSTANCES=`${JAVA_HOME}/bin/jps |grep start.jar|wc -l`
}


jetty_start(){
	$SYS_ECHO "jetty will start now,please wait ..."
	ports=(${JETTY_PORT//,/ })
	x=0;
	for port in ${ports[@]}
	do
	cd ${APP_INSTALL_HOME}/conf/jetty/
	${JAVA_HOME}/bin/java  ${JAVA_DEBUG_OPTS} ${JAVA_OPTS} -DjettyPort=${port} -jar ${JETTY_HOME}/start.jar jetty.port=${port} --ini=start.ini jetty.home=${JETTY_HOME} jetty.extractwars=true >> ${APP_LOG_DIR_PATH}/jetty/jetty-${port}.log 2>&1 &
	x=$(($x+1))
	done
	
	while [ $JETTY_INSTANCES -eq 0 ]
	do
	$SYS_ECHO -n "#"
	get_jetty_instance
	done
	 for((i=1;i<=100;i++));  do $SYS_ECHO -n "#" ; done ;
	sleep 2
	echo ""
	$SYS_ECHO "the jetty is start ready !!!"
}


kill_jetty(){
pids=`${JAVA_HOME}/bin/jps | grep start.jar|awk '{print $1}'`
for pid in $pids
  do
   kill -9 $pid
 done
}

jetty_stop(){
	$SYS_ECHO "now the jetty process will stop ..."
	kill_jetty
	get_jetty_instance
	
	while [ $JETTY_INSTANCES -gt 0 ]
	do
	$SYS_ECHO -n "#"
	get_jetty_instance
	done
	
	for((i=1;i<=100;i++));  do $SYS_ECHO -n "#" ; done ;
	sleep 2
	$SYS_ECHO ""
	$SYS_ECHO "the jetty process is stop success !!!"
}