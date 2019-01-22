#! /bin/bash
##### IMPORTANT : THIS DOES NOT WORK....IS ONLY HERE FOR REFERENCE
if [ $# -ne 1 ]
then
	echo "USAGE gen.sh [apiname]"
	exit 1
fi
# Go to the root directory
cd `dirname $0`/..
echo "Working from" `pwd`

rm -Rf ./apis/$1/client/*
rm -Rf ./apis/$1/doc/*
echo "Builing the template for API /scala-gatling/$1"

APIROOT="./apis/scala-gatling/$1"
SCHEMA="./swagger_schemas/swagger/json/$1.json"
CLIENT_CONFIG="./swagger_schemas/swagger/json/$1-gatlingclient-config.json"

echo "    Client for API $1"
echo " arguments: -i $SCHEMA -l scala-gatling -c $CLIENT_CONFIG -o $APIROOT/client"
java -jar ./libs/swagger-codegen-cli.jar config-help -l scala-gatling
java -jar ./libs/swagger-codegen-cli.jar generate  \
     -i $SCHEMA \
     -l scala-gatling \
     -c $CLIENT_CONFIG \
     -o $APIROOT/client
if [ $? -ne 0 ]
then
     echo "**** Failed ****"
     exit 1
fi
