FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml ./
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress verify

FROM tomcat:11.0-jdk17-temurin
WORKDIR /usr/local/tomcat
COPY --from=build /build/target/sponsorship.war ./webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
