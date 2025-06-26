FROM eclipse-temurin:17-jre

ARG jarToCopy

RUN apt-get update && apt-get install -y \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libxrandr2 \
    fontconfig \
    && (apt-get install -y libasound2 || apt-get install -y libasound2t64) \
    && rm -rf /var/lib/apt/lists/*

COPY target/$jarToCopy /app/app.jar

ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh /
RUN chmod +x wait-for-it.sh

WORKDIR /app

CMD ["/wait-for-it.sh", "postgresdb:5432", "--", "java", "-jar", "app.jar"]