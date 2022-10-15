FROM eclipse-temurin:11 AS build

# Get gradle distribution
COPY *.gradle *.gradle.kts gradle.* gradlew /src/
COPY gradle /src/gradle
WORKDIR /src
RUN ./gradlew --version

# Build the app
COPY . .
RUN ./gradlew shadowJar

# Run the app
FROM eclipse-temurin:11-jre-alpine
EXPOSE 8080:8080
ENV PORT=8080
RUN mkdir /app
COPY --from=build /src/build/libs/*-all.jar /app/app.jar
WORKDIR /app
ENTRYPOINT ["java","-jar","app.jar"]
