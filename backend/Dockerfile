# http://whitfin.io/speeding-up-maven-docker-builds/

FROM maven:3-jdk-11 as maven
COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./src ./src
ARG SPRING_PROFILE=dev
RUN mvn -P${SPRING_PROFILE} -Dmaven.test.skip=true package


FROM adoptopenjdk/openjdk11-openj9:jre-11.0.7_10_openj9-0.20.0

RUN useradd --uid 2000 --system --shell /usr/bin/nologin --no-log-init quizzestutor
USER quizzestutor

EXPOSE 8080

COPY --from=maven target/quizzes-tutor-backend-0.0.1-SNAPSHOT.jar ./
CMD ["java","-Djava.security.egd=file:/dev/./urandom","-jar", "./quizzes-tutor-backend-0.0.1-SNAPSHOT.jar"]

