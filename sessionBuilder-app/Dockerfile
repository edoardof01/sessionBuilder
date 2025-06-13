FROM eclipse-temurin:17-jre

ARG jarToCopy

COPY target/$jarToCopy /app/app.jar

ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh /
RUN chmod +x wait-for-it.sh

WORKDIR /app

CMD ["java", "-jar", "app.jar"]