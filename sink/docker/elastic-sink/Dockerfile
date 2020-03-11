FROM amazoncorretto:11
VOLUME /usr/app
COPY . .
RUN ls -al .
RUN cp ./build/atriangle-elastic-sink.jar atriangle-elastic-sink.jar
RUN ls -al .
ENTRYPOINT ["java","-jar", "atriangle-elastic-sink.jar","-server"]