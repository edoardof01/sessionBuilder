FROM eclipse-temurin:17-jre

ARG jarToCopy

COPY target/$jarToCopy /app/app.jar

WORKDIR /app

CMD ["java", "-jar", "app.jar"]