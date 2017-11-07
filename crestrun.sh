echo "Crest server tool"

#!/bin/sh
### BEGIN INIT INFO
# Provides:          crestserver
# Required-Start:    $CREST_WAR, filename for h2.
# Required-Stop:     
# Default-Start:     local war
# Default-Stop:      
# Description:       <DESCRIPTION>
### END INIT INFO


DBFILE=$2

if [ -z $CREST_PROFILE ]; then
	CREST_PROFILE="default"	
fi

CREST_OPTS="-Dspring.profiles.active=${CREST_PROFILE} -Dcrest.dbfile=$DBFILE"
PIDFILE=./crestserver.pid
LOGFILE=./crestserver.log

if [ -z $CREST_WAR ]; then
	CREST_WAR="./crestdb-web/build/libs/crest.war"	
fi

SCRIPT="java $CREST_OPTS -jar ${CREST_WAR}"
RUNAS=$USER


start() {
  if [ -f ./$PIDFILE ] && kill -0 $(cat ./$PIDFILE); then
    echo 'Service already running' >&2
    return 1
  fi
  echo 'Starting service…' >&2
  CMD=$SCRIPT
  echo "Use command ${CMD}"
  $CMD > $LOGFILE 2>&1 & echo $! > $PIDFILE 
  echo 'Service started' >&2
  echo "Port is 8090 for local profiles and 8080 for production profiles"
  echo "To check if server is running, type : "
  echo "curl http://localhost:8090/crestapi/globaltags"
  echo "You should see an empty result but no errors"
}

stop() {
  if [ ! -f "$PIDFILE" ] || ! kill -0 $(cat "$PIDFILE"); then
    echo 'Service not running' >&2
    return 1
  fi
  echo 'Stopping service…' >&2
  kill -15 $(cat "$PIDFILE") && rm -f "$PIDFILE"
  echo 'Service stopped' >&2
}


case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  restart)
    stop
    start
    ;;
  *)
	echo "Usage: $0 {start|stop|restart} {db file}"
	echo " Set CREST_WAR for a path to an existing war file"
	echo " Set CREST_PROFILE for a database backend: example sqlite will use an sqlite; default is H2 database file."
esac
