#!/bin/sh

if [ $# -ne 1 ] 
then
	echo "USAGE gen.sh [apiname]"
	exit 1 
fi
quite="no"
# Go to the root directory
cd `dirname $0`/..
echo "Working from" `pwd`

APIROOT="./apis/jersey/$1"
echo "Removing directory ./apis/jersey/$1/client/*"
rm -Rf .$APIROOT/client/*
echo "Removing directory ./apis/jersey/$1/server/*"
rm -Rf $APIROOT/server/*
echo "Removing directory ./apis/jersey/$1/doc/*"
rm -Rf $APIROOT/doc/*


echo "Building the template for API /jersey/$1"

if [ ! -d $APIROOT ]; then
	echo "Create directory $APIROOT"
	mkdir -p $APIROOT
fi
SCHEMA="./swagger_schemas/swagger/json/$1.json"
CONFIG="./swagger_schemas/swagger/json/$1-config.json"
CLIENT_CONFIG="./swagger_schemas/swagger/json/$1-client-config.json"

echo "    Client for API $1 using -i $SCHEMA -c $CLIENT_CONFIG -l java -o $APIROOT/client"
if [ $quite == "yes" ]; then
	echo " quite execution...not launching server code generation;"
else
java -jar ./libs/swagger-codegen-cli-2.2.2.jar generate \
     -i $SCHEMA \
     -c $CLIENT_CONFIG \
     -l java \
     -o $APIROOT/client
fi    
if [ $? -ne 0 ]; then
    echo "**** Failed ****"
    return 1
fi

echo "    Server for API $1  using -i $SCHEMA -c $CONFIG -l jaxrs -t ./templates/java/JavaJaxRs -o $APIROOT/server"
if [ $quite == "yes" ]; then
	echo " quite execution...not launching server code generation;"
else
 java -jar  ./libs/swagger-codegen-cli-2.2.2.jar generate \
     -i $SCHEMA \
     -c $CONFIG \
     -l jaxrs \
     -t ./templates/java/JavaJaxRs \
     -o $APIROOT/server 
         ###-t templates/java/factory 
fi

echo "    Doc for API $1"
if [ $quite == "yes" ]; then
	echo " quite execution...not launching server code generation;"
else
 java -jar  ./libs/swagger-codegen-cli-2.2.2.jar generate -i $SCHEMA \
     -l html \
     -o $APIROOT/doc
fi
