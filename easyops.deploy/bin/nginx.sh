#!/usr/bin/env bash

NGINX_PID=0

get_nginx_pid(){
       ps_str=`ps -ef | grep nginx |grep master`
       NGINX_PID=`echo ${ps_str}| awk '{print $2}'`
       if [ -z $NGINX_PID ]; then
       NGINX_PID=0
       fi   
}

nginx_start(){
	if [ $NGINX_PID -gt 0 ];then
	echo "nginx is already start ."
	else
	echo "start nginx now,please wait ..."
	${NGINX_HOME}/sbin/nginx -c ${APP_INSTALL_HOME}/conf/nginx/nginx.conf > ${APP_LOG_DIR_PATH}/nginx/nginx.log 2>&1 &
	NGINX_PID=0
	while [ $NGINX_PID -eq 0 ]
	 	do
	      $SYS_ECHO -n "#"
	      get_nginx_pid
	  done
	  for((i=1;i<=100;i++));  do $SYS_ECHO -n "#" ; done ;
	sleep 2
	echo ""
	echo "start nginx success !!!"
	fi 
}

nginx_stop(){
	$SYS_ECHO "now the nginx process will stop ..."
	get_nginx_pid
	kill -QUIT $NGINX_PID
	
	while [ $NGINX_PID -gt 0 ]
	do
	$SYS_ECHO -n "#"
	get_nginx_pid
	done
	
	for((i=1;i<=100;i++));  do $SYS_ECHO -n "#" ; done ;
	sleep 2
	$SYS_ECHO ""
	$SYS_ECHO "the nginx process is stop success !!!"
}