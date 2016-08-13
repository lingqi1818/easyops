#!/usr/bin/env bash

OS=`uname`
HOST_NAME=`hostname`
SYS_ECHO=echo

if [ $OS == "Darwin" ]; then
SYS_ECHO=/bin/echo
fi