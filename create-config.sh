#!/bin/sh
# This script should be run once in the SWARM environment
docker config rm vhfmgr-conf
docker config rm vhfmgr-log
docker config rm vhfmgr-jopts

docker config create vhfmgr-conf ./config/application.properties.swarm
docker config create vhfmgr-log ./logback.xml.vhfmgr
docker config create vhfmgr-jopts ./javaopt.properties