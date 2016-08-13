#!/usr/bin/env bash

#=========================================
# this script will start the application
# author:chenke
# contact:chenke@dumpcache.com
#=========================================

source ./env.sh
source ./base.sh
source ./nginx.sh
source ./jetty.sh

JETTY_INSTANCES=0

nginx_start

jetty_start
