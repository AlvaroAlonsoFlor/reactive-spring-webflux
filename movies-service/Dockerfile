FROM eclipse-temurin:21-alpine AS base-build

FROM eclipse-temurin:21-jre-alpine AS base-run

FROM base-build AS build

WORKDIR /app

COPY settings.gradle build.gradle gradlew ./
COPY gradle/ ./gradle
RUN ./gradlew --version # cache distribution
COPY src/ ./src
RUN ./gradlew assemble

FROM base-run

COPY --from=build /app/build/libs/movies-service-0.0.1-SNAPSHOT.jar /app.jar

ARG UID=10101
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
USER appuser

ENTRYPOINT ["java","-jar","/app.jar"]