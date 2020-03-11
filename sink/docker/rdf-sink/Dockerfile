FROM amazoncorretto:11
VOLUME /usr/app
COPY . .
RUN cp ./build/atriangle-rdf-sink.jar atriangle-rdf-sink.jar
RUN ls -al .
ENTRYPOINT ["java","-jar", "atriangle-rdf-sink.jar","-server"]