# Image produksi product-service (slide 159). Berbeda dari mode perf (infra/docker/perf.Dockerfile, base -jdk):
# di sini runtime JRE saja, non-root, heap relatif ke limit container.
# Build jar dulu: .\mvnw.cmd -pl product-service package -DskipTests
FROM eclipse-temurin:25-jre AS extract
WORKDIR /app
COPY target/product-service-1.0.0-SNAPSHOT.jar app.jar
# Mode tools (layertools dihapus di Boot 4.1): app.jar + lib/ terpisah.
RUN java -Djarmode=tools -jar app.jar extract --destination out

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=extract /app/out/ ./
USER 10001
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70"
EXPOSE 8080 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

# Alternatif resmi: .\mvnw.cmd -pl product-service spring-boot:build-image (Cloud Native Buildpacks, builder Paketo).
