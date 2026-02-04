FROM eclipse-temurin:21

ADD target/cluster-storage-demo-*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
