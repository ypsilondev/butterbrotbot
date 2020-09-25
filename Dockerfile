FROM openjdk:8
COPY ./data /usr/src/bbbot
WORKDIR /usr/src/bbbot
CMD ["java", "-jar", "bbbot.jar"]