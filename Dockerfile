# CrestDB
#
# VERSION       CrestDB-1.0

# use the centos base image provided by dotCloud
FROM openjdk:8u121-jdk
MAINTAINER Andrea Formica

ENV crest_version 1.0-SNAPSHOT
ENV catalina_base /tmp
ENV gradle_version 4.2.1
ENV TZ GMT
RUN mkdir -p ${catalina_base}/logs

RUN ./gradlew clean :crestdb-web:build
ADD crestdb-web/build/libs/crest.war crest.war

RUN chown -R 1001:0 crest.war

USER 1001

EXPOSE 8080

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar crest.war" ]
