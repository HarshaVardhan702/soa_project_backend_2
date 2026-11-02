FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY eureka-server eureka-server
COPY auth-service auth-service
COPY policy-service policy-service
COPY approval-service approval-service
COPY claim-service claim-service
COPY api-gateway api-gateway
RUN mvn -pl api-gateway -am -DskipTests package
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/api-gateway-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
