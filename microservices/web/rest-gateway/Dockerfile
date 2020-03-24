FROM amazoncorretto:11
VOLUME /usr/app
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
EXPOSE 8088
ENTRYPOINT ["java","-jar", "app.jar","-server"]
