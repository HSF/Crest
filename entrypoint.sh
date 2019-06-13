#!/bin/sh

## -Dlogging.config=/data/logs/logback.xml

echo "Setting JAVA_OPTS from file javaopts.properties"
joptfile=./javaopts.properties
echo "use opt : "
cat $joptfile
if [ -e $joptfile ]; then
   export JAVA_OPTS=
   while read line; do echo $line; JAVA_OPTS="$JAVA_OPTS -D$line"; done < $joptfile
fi
if [ -z "$crest_dir" ]; then
   crest_dir=$PWD/crestdb-web/build/libs
fi 

echo "$USER is starting server with JAVA_OPTS : $JAVA_OPTS from user directory $PWD"
if [ x"$1" = x"" ]; then
    sh -c "java $JAVA_OPTS -jar ${crest_dir}/crest.war"
else
    sh -c "$@"
fi
