#!/usr/bin/env bash
#=========================================
# this script will stop the application
# author:chenke
# contact:chenke@chinamobile.com
#=========================================

source ./env.sh
source ./base.sh
source ./nginx.sh
source ./jetty.sh

nginx_stop

jetty_stop