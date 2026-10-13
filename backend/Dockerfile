# Imagen del backend de Smartbody para la Etapa 6 (despliegue con Docker / Kubernetes).
#
# El jar NO se compila aqui: lo produce el pipeline de Jenkins (fase Package) y queda
# versionado en GitHub Packages. Para construir la imagen se toma ese artefacto:
#
#   docker build -t smartbody-backend:1.0.0 --build-arg JAR_FILE=target/smartbody-backend-1.0.0.jar .
#
# Variables de entorno de la aplicacion: ver README (SMARTBODY_DB_HOST, SMARTBODY_DB_PASSWORD, ...).
# Las claves RSA del JWT se montan como volumen en /app/keys; nunca se copian a la imagen.

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

ARG JAR_FILE=target/smartbody-backend-*.jar
COPY ${JAR_FILE} /app/app.jar

# Usuario sin privilegios; /app/keys es el volumen de las claves RSA.
RUN addgroup -S smartbody && adduser -S smartbody -G smartbody \
    && mkdir -p /app/keys && chown -R smartbody:smartbody /app
USER smartbody

ENV SMARTBODY_KEYS_DIR=/app/keys \
    JAVA_TOOL_OPTIONS="-Xmx384m -Dfile.encoding=UTF-8"

EXPOSE 8080

# Spring Boot Actuator responde {"status":"UP"} cuando la app y la base de datos estan listas.
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
