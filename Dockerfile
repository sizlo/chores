FROM eclipse-temurin:17 AS build

WORKDIR /home

COPY gradlew .
COPY gradle ./gradle
RUN ./gradlew --version

COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .
COPY micronaut-cli.yml .
COPY src ./src

RUN MICRONAUT_ENVIRONMENTS=raspberrypi ./gradlew build

FROM eclipse-temurin:17

COPY --from=build /home/build/libs/chores-*-all-optimized.jar /home/chores.jar

ENTRYPOINT ["java", "-jar", "/home/chores.jar"]
