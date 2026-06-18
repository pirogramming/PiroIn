FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY build/libs/*.jar app.jar

VOLUME /app/uploads

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]