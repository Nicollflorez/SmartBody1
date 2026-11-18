-- ============================================================
-- Smartbody - tabla de predicciones de tiempo hasta el peso objetivo
-- Microservicio progreso (base smartbody_progreso).
-- Migracion idempotente: se puede ejecutar mas de una vez.
-- Ejecutar:  mysql -u root < database/microservicios/04_predicciones.sql
-- ============================================================

USE smartbody_progreso;

CREATE TABLE IF NOT EXISTS predicciones (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    -- Usuario dueno de la prediccion (tabla usuarios en smartbody_auth: sin clave foranea entre esquemas).
    usuario_id         BIGINT       NOT NULL,
    objetivo           VARCHAR(20)  NOT NULL,
    peso_inicial_kg    DECIMAL(5,2) NOT NULL,
    peso_objetivo_kg   DECIMAL(5,2) NOT NULL,
    altura_cm          DECIMAL(5,2) NOT NULL,
    edad               INT          NOT NULL,
    sexo               VARCHAR(12)  NOT NULL,
    semanas_estimadas  DECIMAL(6,1) NOT NULL,
    -- Modelo que genero la estimacion y su version (para saber con que modelo se calculo).
    modelo             VARCHAR(40)  NOT NULL,
    version_modelo     VARCHAR(20)  NOT NULL,
    fecha_prediccion   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_seguimiento ENUM('PENDIENTE','EN_SEGUIMIENTO','OBJETIVO_ALCANZADO') NOT NULL DEFAULT 'PENDIENTE',
    PRIMARY KEY (id),
    KEY ix_predicciones_usuario (usuario_id, fecha_prediccion),
    CONSTRAINT ck_predicciones_objetivo CHECK (objetivo IN ('PERDER_PESO','GANAR_MASA')),
    CONSTRAINT ck_predicciones_semanas  CHECK (semanas_estimadas > 0),
    CONSTRAINT ck_predicciones_peso     CHECK (peso_inicial_kg > 0 AND peso_objetivo_kg > 0)
) ENGINE=InnoDB;
