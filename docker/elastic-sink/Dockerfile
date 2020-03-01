FROM amazoncorretto:11
VOLUME /usr/app
WORKDIR elastic-sink
COPY elastic-sink .
RUN cp target/atriangle-elastic-sink.jar atriangle-elastic-sink.jar
RUN ls -al .
ENTRYPOINT ["java","-jar", "atriangle-elastic-sink.jar","--server"]