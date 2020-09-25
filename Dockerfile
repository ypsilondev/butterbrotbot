FROM openjdk:8
COPY . /usr/src/bbbot
WORKDIR /usr/src/bbbot
CMD ["java", "-jar", "bbbot.jar"]