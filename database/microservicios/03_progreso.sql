-- ============================================================
-- Smartbody - esquema del microservicio progreso
-- historial de medidas
-- Generado a partir de database/01_schema.sql y 02_datos_iniciales.sql
-- ============================================================

DROP DATABASE IF EXISTS smartbody_progreso;
CREATE DATABASE smartbody_progreso
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE smartbody_progreso;

CREATE TABLE medidas (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    usuario_id    BIGINT       NOT NULL,
    peso_kg       DECIMAL(5,2) NOT NULL,
    altura_cm     DECIMAL(5,2) NULL,
    cintura_cm    DECIMAL(5,2) NULL,
    cadera_cm     DECIMAL(5,2) NULL,
    pecho_cm      DECIMAL(5,2) NULL,
    brazo_cm      DECIMAL(5,2) NULL,
    imc           DECIMAL(5,2) NULL,
    registrado_en DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY ix_medidas_usuario_fecha (usuario_id, registrado_en),
    CONSTRAINT ck_medidas_peso    CHECK (peso_kg > 0)
) ENGINE=InnoDB;

-- ---- Datos iniciales (los usuarios viven en smartbody_auth, mismo servidor MySQL) ----
INSERT INTO medidas (usuario_id, peso_kg, altura_cm, cintura_cm, imc, registrado_en)
SELECT u.id, 78.00, 175.00, 92.00, 25.47, DATE_SUB(NOW(), INTERVAL 30 DAY) FROM smartbody_auth.usuarios u WHERE u.email = 'demo@smartbody.com'
UNION ALL
SELECT u.id, 76.50, 175.00, 90.00, 24.98, DATE_SUB(NOW(), INTERVAL 15 DAY) FROM smartbody_auth.usuarios u WHERE u.email = 'demo@smartbody.com'
UNION ALL
SELECT u.id, 75.50, 175.00, 88.50, 24.65, NOW() FROM smartbody_auth.usuarios u WHERE u.email = 'demo@smartbody.com';

INSERT INTO medidas (usuario_id, peso_kg, altura_cm, imc, registrado_en)
SELECT u.id, datos.peso, datos.altura, ROUND(datos.peso / POW(datos.altura / 100, 2), 2),
       DATE_SUB(NOW(), INTERVAL datos.dias_atras DAY)
FROM (
    SELECT 'laura@prueba.smartbody.com' AS email, 70.0 AS peso, 162 AS altura, 60 AS dias_atras UNION ALL
    SELECT 'laura@prueba.smartbody.com', 68.8, 162, 40 UNION ALL
    SELECT 'laura@prueba.smartbody.com', 67.4, 162, 20 UNION ALL
    SELECT 'laura@prueba.smartbody.com', 66.5, 162, 1 UNION ALL
    SELECT 'andres@prueba.smartbody.com', 64.0, 178, 60 UNION ALL
    SELECT 'andres@prueba.smartbody.com', 65.1, 178, 40 UNION ALL
    SELECT 'andres@prueba.smartbody.com', 66.3, 178, 20 UNION ALL
    SELECT 'andres@prueba.smartbody.com', 67.2, 178, 1 UNION ALL
    SELECT 'camila@prueba.smartbody.com', 58.0, 160, 60 UNION ALL
    SELECT 'camila@prueba.smartbody.com', 58.4, 160, 30 UNION ALL
    SELECT 'camila@prueba.smartbody.com', 58.6, 160, 2 UNION ALL
    SELECT 'julian@prueba.smartbody.com', 92.0, 175, 58 UNION ALL
    SELECT 'julian@prueba.smartbody.com', 92.6, 175, 30 UNION ALL
    SELECT 'julian@prueba.smartbody.com', 93.1, 175, 3 UNION ALL
    SELECT 'sofia@prueba.smartbody.com', 55.0, 165, 55 UNION ALL
    SELECT 'sofia@prueba.smartbody.com', 55.2, 165, 25 UNION ALL
    SELECT 'sofia@prueba.smartbody.com', 55.4, 165, 2
) AS datos
JOIN smartbody_auth.usuarios u ON u.email = datos.email;

