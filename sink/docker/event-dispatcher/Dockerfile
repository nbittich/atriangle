FROM amazoncorretto:11
VOLUME /usr/app
COPY . .
RUN cp ./build/atriangle-event-dispatcher.jar atriangle-event-dispatcher.jar
RUN ls -al .
ENTRYPOINT ["java","-jar", "atriangle-event-dispatcher.jar","-server"]