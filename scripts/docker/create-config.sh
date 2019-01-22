#!/bin/sh
# This script should be run once in the SWARM environment
docker config rm crest-conf
docker config rm crest-log
docker config rm crest-jopts

docker config create crest-conf ./config/application.properties.svom
docker config create crest-log ./logback.xml.crest
docker config create crest-jopts ./javaopt.properties