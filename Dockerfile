FROM gradle:7.5-jdk17 AS builder

WORKDIR /app

# Cache dependencies
COPY build.gradle /app/
RUN gradle dependencies --no-daemon

COPY src /app/src
RUN gradle bootJar --no-daemon

FROM azul/zulu-openjdk:17-latest
WORKDIR /app

COPY --from=builder /app/build/libs/lucio-bot.jar /app/lucio-bot.jar

ENTRYPOINT ["java", "-jar", "/app/lucio-bot.jar"]
