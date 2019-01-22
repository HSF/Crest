keytool -genkeypair  -keystore crest.jks
keytool -importcert -alias crest -file cert.crt -keystore crest.jks
