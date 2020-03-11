FROM amazoncorretto:11
VOLUME /usr/app
COPY . .
RUN cp ./build/atriangle-rest-sink.jar atriangle-rest-sink.jar
RUN ls -al .
ENTRYPOINT ["java","-jar", "atriangle-rest-sink.jar","-server"]