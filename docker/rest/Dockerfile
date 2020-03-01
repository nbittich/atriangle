FROM amazoncorretto:11
VOLUME /usr/app
WORKDIR rest
COPY rest .
RUN cp target/atriangle-rest.jar atriangle-rest.jar
RUN ls -al .
EXPOSE 8088
ENTRYPOINT ["java","-jar", "atriangle-rest.jar","--server"]