FROM amazoncorretto:11
VOLUME /usr/app
WORKDIR rest-sink
COPY rest-sink .
RUN cp target/atriangle-rest-sink.jar atriangle-rest-sink.jar
RUN ls -al .
ENTRYPOINT ["java","-jar", "atriangle-rest-sink.jar","-server"]