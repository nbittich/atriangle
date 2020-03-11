FROM amazoncorretto:11
VOLUME /usr/app
COPY . .
RUN cp ./build/atriangle-log-sink.jar atriangle-log-sink.jar
RUN ls -al .
ENTRYPOINT ["java","-jar", "atriangle-log-sink.jar","-server"]