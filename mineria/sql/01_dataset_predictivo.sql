-- ============================================================
-- Smartbody - base de mineria de datos (dataset sintetico)
-- Carga el CSV generado por GeneradorDataset.java.
-- Ejecutar desde la carpeta mineria/ :
--   mysql -u root --local-infile=1 < sql/01_dataset_predictivo.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS smartbody_mineria
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE smartbody_mineria;

DROP TABLE IF EXISTS dataset_predictivo;
CREATE TABLE dataset_predictivo (
    id_registro                  INT          NOT NULL,
    usuario_id                   INT          NOT NULL,
    tipo_registro                VARCHAR(40)  NOT NULL,
    edad                         INT          NULL,
    sexo                         VARCHAR(12)  NULL,
    altura_cm                    DECIMAL(6,1) NULL,
    peso_inicial_kg              DECIMAL(6,1) NULL,
    peso_objetivo_kg             DECIMAL(6,1) NULL,
    diferencia_peso_kg           DECIMAL(6,1) NULL,
    imc_inicial                  DECIMAL(6,2) NULL,
    objetivo                     VARCHAR(20)  NULL,
    nivel_actividad              VARCHAR(12)  NULL,
    nivel_experiencia            VARCHAR(12)  NULL,
    entrenamientos_semana        INT          NULL,
    minutos_sesion               INT          NULL,
    tipo_actividad               VARCHAR(12)  NULL,
    consumo_energetico_kcal      INT          NULL,
    balance_energetico_kcal      INT          NULL,
    restricciones_total          INT          NULL,
    adherencia_nutricional_pct   DECIMAL(5,1) NULL,
    adherencia_ejercicio_pct     DECIMAL(5,1) NULL,
    semanas_estimadas            DECIMAL(6,1) NULL,
    PRIMARY KEY (id_registro),
    KEY ix_dataset_tipo (tipo_registro),
    KEY ix_dataset_usuario (usuario_id)
) ENGINE=InnoDB;

-- Los campos vacios del CSV (datos faltantes) se guardan como NULL.
LOAD DATA LOCAL INFILE 'datos/smartbody_dataset_100000.csv'
INTO TABLE dataset_predictivo
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(id_registro, usuario_id, tipo_registro, edad, sexo, altura_cm, peso_inicial_kg, peso_objetivo_kg,
 diferencia_peso_kg, imc_inicial, objetivo, nivel_actividad, nivel_experiencia, entrenamientos_semana,
 minutos_sesion, tipo_actividad, consumo_energetico_kcal, balance_energetico_kcal, restricciones_total,
 adherencia_nutricional_pct, adherencia_ejercicio_pct, semanas_estimadas)
SET edad = NULLIF(edad, ''),
    altura_cm = NULLIF(altura_cm, ''),
    peso_inicial_kg = NULLIF(peso_inicial_kg, ''),
    peso_objetivo_kg = NULLIF(peso_objetivo_kg, ''),
    diferencia_peso_kg = NULLIF(diferencia_peso_kg, ''),
    imc_inicial = NULLIF(imc_inicial, ''),
    entrenamientos_semana = NULLIF(entrenamientos_semana, ''),
    minutos_sesion = NULLIF(minutos_sesion, ''),
    restricciones_total = NULLIF(restricciones_total, ''),
    adherencia_nutricional_pct = NULLIF(adherencia_nutricional_pct, ''),
    adherencia_ejercicio_pct = NULLIF(adherencia_ejercicio_pct, ''),
    nivel_actividad = NULLIF(nivel_actividad, ''),
    tipo_actividad = NULLIF(tipo_actividad, '');
