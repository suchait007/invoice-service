FROM openjdk:21-jdk
RUN apt-get update && apt-get install -y curl
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]