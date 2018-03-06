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

APIROOT="./apis/qt5cpp/$1"
SCHEMA="./swagger_schemas/swagger/json/$1.json"
##CLIENT_CONFIG="./swagger_schemas/swagger/json/$1-pyclient-config.json"

echo "    Client for API $1"
java -jar ./libs/swagger-codegen-cli-2.2.2.jar config-help -l qt5cpp
java -jar ./libs/swagger-codegen-cli-2.2.2.jar generate  \
     -i $SCHEMA \
     -l qt5cpp \
     -o $APIROOT/client
if [ $? -ne 0 ]
then
     echo "**** Failed ****"
     exit 1
fi
