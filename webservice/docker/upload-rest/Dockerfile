FROM amazoncorretto:11
VOLUME /usr/app
COPY . .
RUN cp build/atriangle-upload-rest.jar atriangle-upload-rest.jar
RUN ls -al .
ENTRYPOINT ["java","-jar", "atriangle-upload-rest.jar","-server"]