FROM amazoncorretto:11
VOLUME /usr/app
WORKDIR upload-rest
COPY upload-rest .
RUN cp target/atriangle-upload-rest.jar atriangle-upload-rest.jar
RUN ls -al .
ENTRYPOINT ["java","-jar", "atriangle-upload-rest.jar","-server"]