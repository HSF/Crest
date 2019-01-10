#### Author: A.Formica, R.Sipos
##### Date of last development period: 2019/01/13
##### Recent additions: new api methods for uploads of iov+payload, a web-ui in vuejs.  
```
   Copyright (C) 2016  A.Formica, R.Sipos

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
```
# Table of Contents
1. [Description](#description)
2. [Installation](#installation)
3. [Build instructions](#build-instructions)
4. [Run the server](#run-the-server)
5. [Swagger](#swagger)
6. [Docker](#docker)
7. [Openshift](#openshift)
8. [Clients](#clients)


## Description
Test project for the implementation of a generic purpose conditions database for physics experiment.
This server was generated by the [swagger-codegen](https://github.com/swagger-api/swagger-codegen) project. By using the 
[OpenAPI-Spec](https://github.com/swagger-api/swagger-core/wiki) from a remote server, you can easily generate a server stub.  This
project is an example of building a swagger-enabled JAX-RS server. Some tests were also done to provide a Resteasy implementation.

The prototype uses [Spring framework](https://spring.io) and the REST services are implemented via  [Jersey](https://jersey.java.net).

The prototype runs as a microservice using `spring-boot`. By default it uses an embedded [undertow](http://undertow.io) servlet container, but others like [tomcat](https://tomcat.apache.org) or [jetty](https://www.eclipse.org/jetty/) can be easily used instead of [undertow](http://undertow.io).


## Installation
Download the project from gitlab (example below is using `https`):
```
git clone https://gitlab.cern.ch/formica/swagger_crestdb.git
```
or
```
git clone https://github.com/HSF/Crest.git
```
if you are taking the github version.
This will create a directory `swagger_crestdb` in the location where you run the git command.

## Build instructions
You need to have java >= 8 installed on your machine. If you have also [gradle](https://gradle.org) (version 5) you can build the project using the following command from the root project directory (`swagger_crestdb`):
```
gradle clean build
```
This command will generate a war (java web archive) file in  : `crestdb-web/build/libs/crest.war`.
In case gradle is not installed on your machine, you can run the wrapper delivered with the project:
```
./gradlew clean build
```

## Run the server
This section is under maintenance.

The server will use by default an embedded `undertow` web server.

The server need by definition to have a database connection in order to store the conditions data. The database connections are defined in the files `./crestdb-web/src/main/resources/application-<profile>.yml`. This file present different set of properties which are chosen by selecting a specific spring profile when running the server. The file should be edited if you are administering the conditions database in order to provide an appropriate set of parameters.

If you do not have any remote database available you should use the default spring profile.

The set of default properties to run the server is defined in `config/application.properties` which will be read by spring when starting the server. The file there will use the `default` spring profile and a local database instance `h2database` where to store the data (it is a sort of `sqlite` file).

To start the server you can simply run:

```
./entrypoint.sh
```
This script is the same that is used by the docker container (when packaging the server via the `Dockerfile`).

We provide the following commands as examples for alternative way (not maintained anymore):
```
cd crestdb-web
$ gradle bootRun "-Dspring.profiles.active=prod" "-Dcrest.db.password=xxx"
```
or
```
$java -Dspring.profiles.active=prod -Dcrest.db.password=xxx -jar crestdb-web/build/libs/crest.war
```
<details>
<summary>Obsolete: click me to collapse/fold.</summary>

>The next section is obsolete. We leave for the moment the instructions but they should probably be ignored. >Later on we may provide something similar for a quick database definition.

For faster start and stop of the service we provide also a script that can be used.
```
./crestrun.sh start dbfilename
```
and
```
./crestrun.sh stop
```
For the moment the script is not very well documented, but it should be easy to configure it at your needs.

</details>



### Activate  security
To activate security you need to build the war file including the key-store. The file should go into <crestdb-web>/src/main/resources together with a complete ldap.properties file in which you need to set the manager password.
These are not detailed instructions, it is more a reminder.

```
java -Dstore.password=xxx -Dkey.password=yyy -Dcrest.db.password=ddd -Dcrest.dump.dir=/data/data/dump -Dcrest.web.static=/data/data/web -Dspring.profiles.active=prod -jar crestdb-web/build/libs/crest.war
```
The prod profile is using CERN ldap. Here is an example of ldap properties.

```
USER_SEARCH_BASE="DC=cern,DC=ch"
USER_DN_PATTERNS="CN={0},OU=Users,DC=cern,DC=ch"
GROUP_SEARCH_BASE="OU=e-groups,OU=Workgroups,DC=cern,DC=ch"
GROUP_SEARCH_FILTER="member={0}"
GROUP_ROLE_ATTRIBUTE=cn
MANAGER_DN="CN=formica,OU=Users,OU=Organic Units,DC=cern,DC=ch"
MANAGER_PASSWORD=xxx
LDAP_AUTHENTICATOR_URL=ldaps://cerndc.cern.ch:636
ACCESS=hasRole('atlas-database')
```
In order to test security you can try to use curl:

```
curl -k -u user:password -X GET https://localhost:8443/crestapi/globaltags
```
The -k should skip verification on the certificate.

In order to connect to the ldap server we need to have the truststore correctly set and with an alias corresponding to the `cerndc.cern.ch` certificate. Some java properties need to be set for this:

```
-Djavax.net.ssl.trustStore=/ssl-crest-server.jks -Djavax.net.ssl.trustStorePassword=xxx -Djavax.net.debug=ssl
```
Be careful that the properties defined in the `application.yml` do not work for the truststore.
In order to add certificates to the truststore you can proceed in the following way:

```
echo -n | openssl s_client -connect cerndc.cern.ch:636 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > /tmp/examplecert.crt
openssl x509 -in /tmp/examplecert.crt -text
```
This will retrieve the server side certificate of the host you want to connect to for authentication.

```
keytool -import -trustcacerts -keystore ./crestdb-web/src/main/resources/ssl-crest-server.jks -storepass xxxxx -noprompt -alias cern -file /tmp/examplecert.crt
```
This instead will add the certificate to the truststore (which in our case is the same file).
The truststore has been created using a command like:

```
keytool -genkey -alias crest_localhost_sslserver -keyalg RSA -keysize 2048 -validity 700 -keypass xxx -storepass xxxx -keystore ssl-crest-server.jks
```

## Swagger
You can view the swagger listing here (hopefully the server will be up!):

```
http://crest-undertow.web.cern.ch/crestapi/swagger.json
```
and if you want to play with the server using the swagger-ui you can access it here:

```
http://crest-undertow.web.cern.ch/ext/web/ui/index.html
```

Note that in principle you can get the same links working (a part from the hostname) if you run the server locally.

### Swagger code generation
In order to regenerate the API we use the JSON schemas and templates which are store in the directories:

```
./swagger_schemas
./templates
```

To run code generation some scripts can be used as examples (`./scripts`).
The server stub generation is implemented as well as a gradle task:

```
./gradlew generateSwaggerCode
```

## Docker
You can build a container using

```
docker build -t crest:1.0 .
```
You can run the container using

```
docker run --env-file .environment -p 8080:8080 -d crest:1.0
```
or

```
docker run --env-file .environment -p 8080:8080 -v /mnt/data/dump:/data/dump -v /mnt/data/web:/data/web --net=host -d crest:test
```
In the last example we have been mounting external volumes. These are useful for the swagger-ui and the possibility to dump a tag in a file system based structure. You can use the swagger-ui version that is provided within this project in the directory

```
./web/ui/
```
A special note about the file `.environment` . You need to have this file to set variables which are used at the startup of the server. Some of the variables are already provided in the version in git, but other are not. For example, to access Oracle at CERN (for the moment only integration cluster contains a crest schema) you need to have the variable `crest.db.password=xxxxx` correctly set for a writer account. 
If you use `spring.profiles.active=default` you will have an h2 database created in `jdbc:h2:/tmp/cresth2;DB_CLOSE_ON_EXIT=FALSE`.

You can connect to a running container using commands like:

```
docker exec -i -t infallible_stonebraker /bin/bash
```
## Openshift
We gather here some notes on openshift deployment via gitlab-ci. These notes are for usage inside CERN.
### Constraints
For the moment in order for the deployment to work we need to have a public access to the gitlab project.
### Problems
After committing a tag it seems that the deploy to openshift fails.
TO BE DONE.

## Clients
We have been merging our clients in this repository with the contribution of some colleagues from *Juno* collaboration. We have now work in progress in the following areas (available code can be seen in *crestdb-client* repository). 
> This is work in progress...documentation needs to be improved....

### Python
Client is generated via swagger. (complete here)
### C++
Ask Juno colleagues.
### gatling
Generated via swagger. Used for testing REST API.
### qt5cpp
Generated via swagger. This is just a demo.

In addition we have recently added a Web GUI in *VueJS*. The project can be found in *web-ui/crest-ui*.
In order to run it one can simply follow the readme file. It can use for development purpose *npm* and *node*.