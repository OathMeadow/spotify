FROM openjdk:11

ENV SERVER_HOME /usr/verticles

RUN mkdir $SERVER_HOME

COPY spotify.zip /tmp
COPY docker-entrypoint.sh /usr/local/bin/
RUN unzip /tmp/spotify.zip -d /tmp && \
    mv /tmp/spotify-*/* $SERVER_HOME && \
    rm -R /tmp/*

RUN chmod 0755 /usr/local/bin/docker-entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["docker-entrypoint.sh"]
