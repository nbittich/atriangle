FROM amazoncorretto:11
VOLUME /usr/app
COPY . .
RUN cp build/atriangle-rest.jar atriangle-rest.jar
RUN ls -al .
EXPOSE 8088
ENTRYPOINT ["java","-jar", "atriangle-rest.jar","-server"]