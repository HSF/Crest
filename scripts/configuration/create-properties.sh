#!/bin/sh
if [ ! -f ./application.properties ]; then
	touch ./application.properties
fi
if [ -d /run/secrets/ ]; then
    for filename in /run/secrets/*; do
        echo "Found secret $filename"
#        echo "${filename##*/}=`cat $filename`" >> ./application.properties
    done
    echo "Added secrets to application.properties"
    cat ./application.properties
fi
