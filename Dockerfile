FROM maven:3.5.3-jdk-8 as builder

WORKDIR /app

COPY . /app
COPY docker-configs/simpatico.properties src/main/resources/simpatico.properties

RUN mvn clean package


FROM tomcat:7

COPY --from=builder /app/target/simpatico.war /usr/local/tomcat/webapps/simpatico.war

EXPOSE 8080
