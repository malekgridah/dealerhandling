# Builder Stage
FROM eclipse-temurin:17 as builder
WORKDIR /opt/builder
ARG ARTIFACT_NAME
COPY ${ARTIFACT_NAME}.war ${ARTIFACT_NAME}.war
RUN java -Djarmode=layertools -jar ${ARTIFACT_NAME}.war extract

# Final Stage
FROM eclipse-temurin:17
WORKDIR /opt/application
ARG EXPOSED_PORT
EXPOSE ${EXPOSED_PORT}
ENV SPRING_PROFILES_ACTIVE=docker,internal,decrypt

# Copy the extracted layers from the builder stage
COPY --from=builder /opt/builder/dependencies/ ./
COPY --from=builder /opt/builder/spring-boot-loader/ ./
COPY --from=builder /opt/builder/snapshot-dependencies/ ./
COPY --from=builder /opt/builder/application/ ./

# Run the application using the WAR launcher
ENTRYPOINT ["java", "org.springframework.boot.loader.WarLauncher"]
