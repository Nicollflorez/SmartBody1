# Etapa 1: Compilar el proyecto con Maven dentro de Render
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Etapa 2: Ejecutar la aplicación ligera y cuidar la memoria RAM
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/smartbody-backend-*.jar /app/app.jar

RUN addgroup -S smartbody && adduser -S smartbody -G smartbody \
    && mkdir -p /app/keys && chown -R smartbody:smartbody /app
USER smartbody

ENV SMARTBODY_KEYS_DIR=/app/keys \
    JAVA_TOOL_OPTIONS="-Xmx384m -Dfile.encoding=UTF-8"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]