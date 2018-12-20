#!/bin/sh
docker secret rm vhftruststore_password
echo $1 | docker secret create vhftruststore_password -

docker secret rm vhfkeystore_password
echo $1 | docker secret create vhfkeystore_password -

docker secret rm vhfmgr-truststore
docker secret rm vhfmgr-keystore
docker secret create vhfmgr-truststore ./certificates/truststore.jks
docker secret create vhfmgr-keystore ./config/keystore.jks
