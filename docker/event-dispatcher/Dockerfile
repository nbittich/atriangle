FROM amazoncorretto:11
VOLUME /usr/app
WORKDIR event-dispatcher
COPY event-dispatcher .
RUN cp target/atriangle-event-dispatcher.jar atriangle-event-dispatcher.jar
RUN ls -al .
ENTRYPOINT ["java","-jar", "atriangle-event-dispatcher.jar","--server"]