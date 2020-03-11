FROM amazoncorretto:11
VOLUME /usr/app
WORKDIR log-sink
COPY log-sink .
RUN cp target/atriangle-log-sink.jar atriangle-log-sink.jar
RUN ls -al .
ENTRYPOINT ["java","-jar", "atriangle-log-sink.jar","-server"]