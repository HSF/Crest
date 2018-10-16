#openssl pkcs12 -in $1.p12 -nocerts -out $1.key.pem -nodes
#openssl pkcs12 -in $1.p12 -clcerts -nokeys -out $1.cert.pem
#cat $1.cert.pem $1.key.pem >> $1.client.pem
# the following lines are used for truststore
TRUSTSTORE=./crest-truststore.jks
PASSWORD=$1
CERNCA="CERN Certification Authority.crt"
CERNROOTCA="CERN Root Certification Authority 2.crt"
keytool -import -trustcacerts -noprompt -alias cern-ca -file "$CERNCA" \
    -keystore $TRUSTSTORE -storepass $PASSWORD
keytool -import -trustcacerts -noprompt -alias cern-root-ca -file "$CERNROOTCA" \
    -keystore $TRUSTSTORE -storepass $PASSWORD


