# CrestDB
#
# VERSION       CrestDB-1.0

# use the centos base image provided by dotCloud
FROM openjdk:8u121-jdk
MAINTAINER Andrea Formica

ENV crest_version 1.0-SNAPSHOT
ENV crest_dir /opt/swagger_crest
ENV catalina_base /tmp
ENV gradle_version 4.2.1
ENV TZ GMT
RUN mkdir -p ${catalina_base}/logs
RUN mkdir -p ${crest_dir}

### This is if you need to create a full war on docker
#ADD . ${crest_dir}
#RUN cd /opt/swagger_crest && ./gradlew clean :crestdb-web:build && cp ./crestdb-web/build/libs/crest.war ${crest_dir}/

## This works if using an externally generated war, in the local directory
ADD crestdb-web/build/libs/crest.war ${crest_dir}/crest.war

RUN chown -R 1001:0 ${crest_dir}/crest.war
RUN chown -R 1001:0 ${catalina_base}
RUN chown -R 1001:0 ${crest_dir}

USER 1001

EXPOSE 8080

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar ${crest_dir}/crest.war" ]
