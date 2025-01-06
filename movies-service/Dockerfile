FROM eclipse-temurin:21-alpine AS build

WORKDIR /app

COPY . .
RUN ./gradlew assemble

FROM eclipse-temurin:21-alpine

COPY --from=build /app/build/libs/movies-service-0.0.1-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]