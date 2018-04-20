# CrestDB
#
# VERSION       CrestDB-1.0

# use the centos base image provided by dotCloud
# FROM openjdk:8u121-jdk
FROM anapsix/alpine-java
MAINTAINER Andrea Formica

ENV crest_version 1.0-SNAPSHOT
ENV crest_dir /opt/swagger_crest
ENV data_dir /data
ENV gradle_version 4.2.1
ENV TZ GMT
RUN mkdir -p ${data_dir}/logs
RUN mkdir -p ${crest_dir}
RUN mkdir -p ${data_dir}/web
RUN mkdir -p ${data_dir}/dump

### This is if you need to create a full war on docker
#ADD . ${crest_dir}
#RUN cd /opt/swagger_crest && ./gradlew clean :crestdb-web:build && cp ./crestdb-web/build/libs/crest.war ${crest_dir}/

## This works if using an externally generated war, in the local directory
ADD crestdb-web/build/libs/crest.war ${crest_dir}/crest.war
ADD web ${data_dir}/web

RUN chown -R 1001:0 ${crest_dir}/crest.war
RUN chown -R 1001:0 ${crest_dir}
RUN chown -R 1001:0 ${data_dir}

USER 1001

VOLUME "/data/web"
VOLUME "/data/dump"
VOLUME "/data/logs"

EXPOSE 8080

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar ${crest_dir}/crest.war" ]
