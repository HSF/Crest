#!/bin/sh

## -Dlogging.config=/data/logs/logback.xml
if [ x"$1" = x"" ]; then
    sh -c "java $JAVA_OPTS -jar ${nsw_dir}/crest.war"
else
    sh -c "$@"
fi