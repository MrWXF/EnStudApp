
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY enstud-gateway/target/*.jar app.jar
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
