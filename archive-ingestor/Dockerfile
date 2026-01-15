FROM openjdk:25-ea-oracle

COPY ./target/reports/apidocs /usr/src/app/docs
COPY ./target/archive-ingestor-0.3.1.jar /usr/src/app/archive-ingestor.jar

WORKDIR /usr/src/app

ENTRYPOINT ["java", "-jar", "archive-ingestor.jar"]
