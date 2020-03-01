FROM amazoncorretto:11
VOLUME /usr/app
WORKDIR elastic-sink
COPY virtuoso-sink .
RUN cp target/atriangle-rdf-sink.jar atriangle-rdf-sink.jar
RUN ls -al .
ENTRYPOINT ["java","-jar", "atriangle-rdf-sink.jar","--server"]