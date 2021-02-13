#
# Build stage
#
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

#
# Package stage
#
FROM openjdk:11-jre-slim
COPY --from=build /home/app/target/bbbot.jar /usr/local/lib/bbbot.jar
COPY --from=build /home/app/target/lib/*  /usr/local/lib/lib/
VOLUME ["/usr/local/lib/data"]
ENTRYPOINT ["java","-jar","/usr/local/lib/bbbot.jar"]