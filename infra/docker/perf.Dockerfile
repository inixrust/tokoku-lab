# Image mode perf (Hari 5-6). Base -jdk (bukan -jre) supaya jcmd dan jfr tersedia di dalam container.
# Build jar dulu di host: .\mvnw.cmd package -DskipTests
ARG JDK=21
FROM eclipse-temurin:${JDK}-jdk AS extract
WORKDIR /build
COPY target/*-SNAPSHOT.jar app.jar
# Extract: classpath jar + lib/ (prasyarat CDS / AOT cache, slide 120).
RUN java -Djarmode=tools -jar app.jar extract --destination /app

FROM eclipse-temurin:${JDK}-jdk
WORKDIR /app
COPY --from=extract /app /app
EXPOSE 8080 8081
# exec form: java menjadi PID 1 dan menerima SIGTERM (graceful shutdown).
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
