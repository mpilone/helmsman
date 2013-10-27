#!/bin/bash

#####################
# Simple wrapper for the Helmsman Java application.
#
# This script assumes the following directory structure but 
# it can be modified as needed.
# 
# helmsman/
#   bin/
#     helmsman.sh
#   config/
#     base.properties
#     my-host.properties
#   lib/
#     helmsman.jar
#

basedir=`dirname $0`

java -Djava.awt.headless=true -client -Dbasedir=$basedir -jar $basedir/../lib/helmsman.jar $@
exit $?