FROM eclipse-temurin:21-alpine AS base

FROM base AS dev


FROM base AS build

WORKDIR /app

COPY . .
RUN ./gradlew assemble

FROM base

COPY --from=build /app/build/libs/movies-service-0.0.1-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]