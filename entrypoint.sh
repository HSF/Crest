#!/bin/sh

## -Dlogging.config=/data/logs/logback.xml
if [ x"$1" = x"" ]; then
    sh -c "java $JAVA_OPTS -jar ${crest_dir}/crest.war"
else
    sh -c "$@"
fi
