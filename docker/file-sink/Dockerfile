FROM amazoncorretto:11
VOLUME /usr/app
WORKDIR file-sink
COPY file-sink .
RUN cp target/atriangle-file-sink.jar atriangle-file-sink.jar
RUN ls -al .
ENTRYPOINT ["java","-jar", "atriangle-file-sink.jar","--server"]