# Imagen de un microservicio de Smartbody. El jar lo produce el pipeline (no se recompila aqui).
#
#   docker build -t smartbody-auth:1.0.4 --build-arg SERVICIO=auth --build-arg VERSION=1.0.4-SNAPSHOT services/
#
# Las claves del JWT se montan en /app/keys: auth necesita private_key.pem y public_key.pem;
# los demas servicios solo public_key.pem.

FROM eclipse-temurin:17-jre-alpine

ARG SERVICIO
ARG VERSION
WORKDIR /app

COPY ${SERVICIO}/target/smartbody-${SERVICIO}-${VERSION}.jar /app/app.jar

RUN addgroup -S smartbody && adduser -S smartbody -G smartbody \
    && mkdir -p /app/keys && chown -R smartbody:smartbody /app
USER smartbody

ENV SMARTBODY_KEYS_DIR=/app/keys \
    JAVA_TOOL_OPTIONS="-Xmx256m -Dfile.encoding=UTF-8"

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
