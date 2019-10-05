# CrestDB
#
# VERSION       CrestDB-1.0

# use the centos base image provided by dotCloud
# FROM openjdk:8u121-jdk
FROM anapsix/alpine-java
MAINTAINER Andrea Formica

ENV USR crest

ENV crest_version 1.1-SNAPSHOT
ENV crest_dir /home/${USR}/swagger_crest
##ENV data_dir /home/${USR}/data
ENV data_dir /data
ENV gradle_version 5.2.1
ENV TZ GMT

RUN  mkdir -p ${crest_dir} \
  && mkdir -p ${data_dir}/web \
  && mkdir -p ${data_dir}/dump \
  && mkdir -p ${data_dir}/logs \
  && mkdir -p ${data_dir}/configs

### This is if you need to create a full war on docker
#ADD . ${crest_dir}
#RUN cd /opt/swagger_crest && ./gradlew clean :crestdb-web:build && cp ./crestdb-web/build/libs/crest.war ${crest_dir}/

## This works if using an externally generated war, in the local directory
ADD crestdb-web/build/libs/crest.war ${crest_dir}/crest.war
ADD web ${crest_dir}/web
#ADD logback.xml.crest ${data_dir}/logback.xml
COPY ./web-ui/crest-ui/dist ${crest_dir}/web/crestui

#VOLUME "${data_dir}/web"
#VOLUME "${data_dir}/dump"
#VOLUME "${data_dir}/logs"
VOLUME "${data_dir}"

EXPOSE 8080

COPY ./entrypoint.sh /home/${USR}
COPY ./create-properties.sh /home/${USR}
WORKDIR /home/${USR}

ENTRYPOINT  [ "./entrypoint.sh" ]
