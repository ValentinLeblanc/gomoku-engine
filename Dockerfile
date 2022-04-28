#
# Package stage
#
FROM openjdk:latest
COPY ./target/gomoku-engine-0.0.1-SNAPSHOT.jar /usr/local/lib/gomoku-engine.jar
EXPOSE 9000
ENTRYPOINT ["java","-jar","/usr/local/lib/gomoku-engine.jar"]