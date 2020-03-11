FROM amazoncorretto:11
VOLUME /usr/app
COPY . .
RUN cp ./build/atriangle-mongodb-sink.jar atriangle-mongodb-sink.jar
RUN ls -al .
ENTRYPOINT ["java","-jar", "atriangle-mongodb-sink.jar","-server"]