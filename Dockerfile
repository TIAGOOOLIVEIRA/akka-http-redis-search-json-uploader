FROM openjdk:8-alpine
LABEL @tiagoooliveira ti.olive@gmail.com

ADD target/app_-0.0.1-SNAPSHOT-standalone.jar /app_/app.jar

CMD ["java", "-jar", "/app_/app.jar"]