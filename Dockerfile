# CrestDB
FROM registry.cern.ch/docker.io/eclipse-temurin:23-alpine
LABEL maintainer="Andrea Formica"

ENV USR=crestsvc
ENV CREST_GID=208

ENV crest_version=1.0-SNAPSHOT
ENV crest_dir=/home/${USR}/crest
ENV data_dir=/home/${USR}/data
ENV config_dir=/home/${USR}/config
ENV TZ=GMT

## RUN groupadd -g 208 crest && adduser -u $CREST_GID -g $CREST_GID -d /home/${USR} ${USR} && usermod -aG crest ${USR}
RUN addgroup -g $CREST_GID crest \
    && adduser -u $CREST_GID -G crest -h /home/${USR} -D ${USR} \
    && addgroup ${USR} crest

RUN  mkdir -p ${crest_dir} \
  && mkdir -p ${config_dir} \
  && mkdir -p ${data_dir}/web \
  && mkdir -p ${data_dir}/dump \
  && mkdir -p ${data_dir}/logs \
  && chown -R ${CREST_GID}:${CREST_GID} /home/${USR}

## This works if using an externally generated war, in the local directory
ADD build/libs/crest.jar ${crest_dir}/crest.jar
## ADD web ${data_dir}/web

### we export only 1 directories....
VOLUME "${data_dir}"
EXPOSE 8080

# copy the entrypoint
COPY ./entrypoint.sh /home/${USR}
COPY ./logback.xml.crest /home/${USR}/logback.xml
## This is not needed in swarm deployment, only for local testing.
COPY ./javaopts.properties /home/${USR}
#COPY ./create-properties.sh /home/${USR}

RUN chown -R $USR:$CREST_GID /home/${USR}

### we set the user and the workdir....
USER ${USR}
WORKDIR /home/${USR}

ENTRYPOINT  [ "./entrypoint.sh" ]
