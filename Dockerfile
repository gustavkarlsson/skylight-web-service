FROM gradle:7-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar --no-daemon

FROM azul/zulu-openjdk-alpine:11-latest
EXPOSE 8080:8080
ENV PORT=8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*-all.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
