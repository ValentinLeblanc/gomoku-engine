FROM openjdk:21-jdk
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} gomoku-engine.jar
ENTRYPOINT ["java","-jar","/gomoku-engine.jar"]