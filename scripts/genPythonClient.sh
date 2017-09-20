#! /bin/bash

if [ $# -ne 1 ] 
then
	echo "USAGE apigen.bash [apiname]"
	exit 1 
fi
# Go to the root directory
cd `dirname $0`/..
echo "Working from" `pwd`

rm -Rf ./apis/$1/client/*
rm -Rf ./apis/$1/doc/*
echo "Builing the template for API /connexion/$1"

APIROOT="./apis/python/$1"
SCHEMA="./swagger_schemas/swagger/json/$1.json"
CLIENT_CONFIG="./swagger_schemas/swagger/json/$1-pyclient-config.json"

echo "    Client for API $1"
java -jar ./libs/swagger-codegen-cli-2.2.2.jar config-help -l python
java -jar ./libs/swagger-codegen-cli-2.2.2.jar generate  \
     -i $SCHEMA \
     -l python \
     -c $CLIENT_CONFIG \
     -o $APIROOT/client
if [ $? -ne 0 ]
then
     echo "**** Failed ****"
     exit 1
fi

echo "    Doc for API $1"
java -jar  ./libs/swagger-codegen-cli-2.2.2.jar generate \
     -DsupportPython2=true \
     -i $SCHEMA \
     -l html \
     -o $APIROOT/doc

echo "    scan Mustache $1"

# to get the mustache keys (stdout does not work : logger issue
# java  -jar  ./libs/swagger-codegen-cli-2.2.2.jar generate \
#     -i $SCHEMA \
#     -l python-flask 2>&1 | grep 4j

# reminders in case of
#java -jar  ./libs/swagger-codegen-cli-2.2.1.jar help generate
#java -jar  ./libs/swagger-codegen-cli-2.2.1.jar config-help -l python-flask
#-c ./config/serverConf.json 
