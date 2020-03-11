FROM amazoncorretto:11
VOLUME /usr/app
WORKDIR mongodb-sink
COPY elastic-sink .
RUN cp target/atriangle-mongodb-sink.jar atriangle-mongodb-sink.jar
RUN ls -al .
ENTRYPOINT ["java","-jar", "atriangle-mongodb-sink.jar","-server"]