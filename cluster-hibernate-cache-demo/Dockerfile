# TODO: Add once depdendencies are reachable
# Build the app
#FROM maven:3.9.12-eclipse-temurin-21 AS build
#ENV HOME=/usr/app
#RUN mkdir -p $HOME
#WORKDIR $HOME
#ADD . $HOME
#RUN --mount=type=cache,target=/root/.m2 mvn clean package

# Package and Run
FROM eclipse-temurin:21
#COPY --from=build /usr/app/target/cluster-storage-demo-*.jar ./app.jar
ADD target/cluster-storage-demo-*.jar ./app.jar
ENTRYPOINT ["java", "--add-exports", "java.base/jdk.internal.misc=ALL-UNNAMED", "-jar", "app.jar"]
