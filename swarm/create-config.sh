#!/bin/sh
# This script should be run once in the SWARM environment
docker secret rm crest-config
docker config rm crest-logs
docker config rm crest-jopts

docker secret create crest-config ./application.properties.crest
docker config create crest-log ./logback.xml.crest
docker config create crest-jopts ./javaopts.properties
