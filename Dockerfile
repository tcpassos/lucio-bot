FROM gradle:7.5-jdk17 AS builder

WORKDIR /app

COPY build.gradle /app/
COPY src /app/src

RUN gradle bootJar

FROM azul/zulu-openjdk:17-latest
WORKDIR /app

COPY --from=builder /app/build/libs/lucio-bot.jar /app/lucio-bot.jar

ENTRYPOINT ["java", "-jar", "/app/lucio-bot.jar"]
