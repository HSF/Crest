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
RUN mkdir -p /opt/gradle && wget https://services.gradle.org/distributions/gradle-${gradle_version}-bin.zip -O /opt/gradle/gradle-${gradle_version}-bin.zip 
RUN ls -altr /opt/gradle
RUN unzip -d /opt/gradle /opt/gradle/gradle-${gradle_version}-bin.zip 
ENV GRADLE_HOME /opt/gradle/gradle-${gradle_version}/bin 
ENV PATH $PATH:/opt/gradle/gradle-${gradle_version}/bin

ADD crestdb-web/build/libs/crestdb-web-${crest_version}.war crest.war

RUN chown -R 1001:0 crest.war
RUN chown -R 1001:0 ${catalina_base}

USER 1001

EXPOSE 8080

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar crest.war" ]
