FROM maven:3.9.6-eclipse-temurin-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /app/target/uno-cli.jar ./uno-cli.jar
ENTRYPOINT ["java", "-jar", "uno-cli.jar"]
CMD ["--bots", "3", "--games", "1", "--quiet"]