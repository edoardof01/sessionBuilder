FROM eclipse-temurin:17-jre

ARG jarToCopy

RUN apt-get update && apt-get install -y \
   libxext6 \
   libxrender1 \
   libxtst6 \
   libxi6 \
   libxrandr2 \
   libasound2 \
   fontconfig \
   && rm -rf /var/lib/apt/lists/*
   
COPY target/$jarToCopy /app/app.jar

ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh /
RUN chmod +x wait-for-it.sh

WORKDIR /app

CMD ["java", "-jar", "app.jar"]