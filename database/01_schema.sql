-- ============================================================
-- Smartbody - Esquema de base de datos
-- Motor: MySQL / MariaDB (XAMPP + phpMyAdmin)
-- Ejecutar antes que 02_datos_iniciales.sql
-- ============================================================

DROP DATABASE IF EXISTS smartbody_db;
CREATE DATABASE smartbody_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE smartbody_db;

-- ------------------------------------------------------------
-- Seguridad y usuarios
-- ------------------------------------------------------------
CREATE TABLE roles (
    id     BIGINT       NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(30)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_roles_nombre (nombre)
) ENGINE=InnoDB;

CREATE TABLE usuarios (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_email (email)
) ENGINE=InnoDB;

-- Foto de perfil en tabla aparte: la imagen solo se lee cuando se pide explicitamente.
CREATE TABLE fotos_perfil (
    usuario_id     BIGINT      NOT NULL,
    contenido      MEDIUMBLOB  NOT NULL,
    tipo_contenido VARCHAR(30) NOT NULL,
    actualizada_en DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id),
    CONSTRAINT fk_fotos_perfil_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE usuario_roles (
    usuario_id BIGINT NOT NULL,
    rol_id     BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usuario_roles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_roles_rol     FOREIGN KEY (rol_id)     REFERENCES roles (id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Catalogos: objetivos y restricciones
-- ------------------------------------------------------------
CREATE TABLE objetivos (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    codigo      VARCHAR(40)  NOT NULL,
    nombre      VARCHAR(80)  NOT NULL,
    descripcion VARCHAR(255) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_objetivos_codigo (codigo)
) ENGINE=InnoDB;

CREATE TABLE restricciones (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    codigo      VARCHAR(40)  NOT NULL,
    tipo        ENUM('SALUD','ALIMENTACION') NOT NULL,
    nombre      VARCHAR(80)  NOT NULL,
    descripcion VARCHAR(255) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_restricciones_codigo (codigo),
    KEY ix_restricciones_tipo (tipo)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Perfil fisico del usuario (RF03, RF04, RF05)
-- ------------------------------------------------------------
CREATE TABLE perfiles (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    usuario_id            BIGINT       NOT NULL,
    edad                  INT          NULL,
    sexo                  ENUM('MASCULINO','FEMENINO','OTRO') NULL,
    peso_kg               DECIMAL(5,2) NULL,
    altura_cm             DECIMAL(5,2) NULL,
    nivel_experiencia     ENUM('PRINCIPIANTE','INTERMEDIO','AVANZADO') NULL,
    minutos_por_sesion    INT          NULL,
    dias_por_semana       INT          NULL,
    objetivo_id           BIGINT       NULL,
    actualizado_en        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_perfiles_usuario (usuario_id),
    CONSTRAINT fk_perfiles_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_perfiles_objetivo FOREIGN KEY (objetivo_id) REFERENCES objetivos (id),
    CONSTRAINT ck_perfiles_edad     CHECK (edad IS NULL OR (edad BETWEEN 12 AND 100)),
    CONSTRAINT ck_perfiles_peso     CHECK (peso_kg IS NULL OR peso_kg > 0),
    CONSTRAINT ck_perfiles_altura   CHECK (altura_cm IS NULL OR altura_cm > 0),
    CONSTRAINT ck_perfiles_dias     CHECK (dias_por_semana IS NULL OR (dias_por_semana BETWEEN 1 AND 7)),
    CONSTRAINT ck_perfiles_minutos  CHECK (minutos_por_sesion IS NULL OR (minutos_por_sesion BETWEEN 15 AND 180))
) ENGINE=InnoDB;

CREATE TABLE usuario_restricciones (
    usuario_id     BIGINT NOT NULL,
    restriccion_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, restriccion_id),
    CONSTRAINT fk_usuario_restr_usuario FOREIGN KEY (usuario_id)     REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_restr_restr   FOREIGN KEY (restriccion_id) REFERENCES restricciones (id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Catalogo de ejercicios (RF06, RF08, RF09)
-- ------------------------------------------------------------
CREATE TABLE ejercicios (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    nombre                VARCHAR(100)  NOT NULL,
    descripcion           VARCHAR(500)  NULL,
    grupo_muscular        VARCHAR(60)   NOT NULL,
    equipamiento          VARCHAR(60)   NOT NULL DEFAULT 'NINGUNO',
    tipo                  ENUM('FUERZA','CARDIO','MOVILIDAD') NOT NULL,
    nivel_minimo          ENUM('PRINCIPIANTE','INTERMEDIO','AVANZADO') NOT NULL DEFAULT 'PRINCIPIANTE',
    duracion_estimada_seg INT           NOT NULL DEFAULT 180,
    instrucciones         VARCHAR(1000) NULL,
    imagen_url            VARCHAR(255)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ejercicios_nombre (nombre),
    KEY ix_ejercicios_grupo (grupo_muscular),
    KEY ix_ejercicios_tipo (tipo)
) ENGINE=InnoDB;

-- Ejercicios contraindicados para una restriccion de salud
CREATE TABLE ejercicio_restricciones (
    ejercicio_id   BIGINT NOT NULL,
    restriccion_id BIGINT NOT NULL,
    PRIMARY KEY (ejercicio_id, restriccion_id),
    CONSTRAINT fk_ejer_restr_ejercicio FOREIGN KEY (ejercicio_id)   REFERENCES ejercicios (id) ON DELETE CASCADE,
    CONSTRAINT fk_ejer_restr_restr     FOREIGN KEY (restriccion_id) REFERENCES restricciones (id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Catalogo de alimentos (plan nutricional)
-- ------------------------------------------------------------
CREATE TABLE alimentos (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(100) NOT NULL,
    grupo           VARCHAR(60)  NOT NULL,
    calorias_100g   DECIMAL(6,2) NOT NULL,
    proteina_g      DECIMAL(5,2) NOT NULL DEFAULT 0,
    carbohidratos_g DECIMAL(5,2) NOT NULL DEFAULT 0,
    grasas_g        DECIMAL(5,2) NOT NULL DEFAULT 0,
    descripcion     VARCHAR(255) NULL,
    preparacion     VARCHAR(500) NULL,
    icono           VARCHAR(16)  NULL,
    -- Momentos del dia en que se recomienda (DESAYUNO,ALMUERZO,CENA,SNACK). NULL: cualquiera.
    momentos        VARCHAR(60)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_alimentos_nombre (nombre),
    KEY ix_alimentos_grupo (grupo)
) ENGINE=InnoDB;

-- Alimentos incompatibles con una restriccion alimentaria
CREATE TABLE alimento_restricciones (
    alimento_id    BIGINT NOT NULL,
    restriccion_id BIGINT NOT NULL,
    PRIMARY KEY (alimento_id, restriccion_id),
    CONSTRAINT fk_alim_restr_alimento FOREIGN KEY (alimento_id)    REFERENCES alimentos (id) ON DELETE CASCADE,
    CONSTRAINT fk_alim_restr_restr    FOREIGN KEY (restriccion_id) REFERENCES restricciones (id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Rutinas generadas (RF06, RF07)
-- ------------------------------------------------------------
CREATE TABLE rutinas (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    usuario_id         BIGINT       NOT NULL,
    objetivo_id        BIGINT       NOT NULL,
    nombre             VARCHAR(120) NOT NULL,
    dias_por_semana    INT          NOT NULL,
    minutos_por_sesion INT          NOT NULL,
    activa             BOOLEAN      NOT NULL DEFAULT TRUE,
    generada_en        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY ix_rutinas_usuario (usuario_id, activa),
    CONSTRAINT fk_rutinas_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_rutinas_objetivo FOREIGN KEY (objetivo_id) REFERENCES objetivos (id)
) ENGINE=InnoDB;

CREATE TABLE rutina_ejercicios (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    rutina_id    BIGINT NOT NULL,
    ejercicio_id BIGINT NOT NULL,
    dia_numero   INT    NOT NULL,
    orden        INT    NOT NULL,
    series       INT    NOT NULL DEFAULT 3,
    repeticiones INT    NULL,
    duracion_seg INT    NULL,
    descanso_seg INT    NOT NULL DEFAULT 60,
    PRIMARY KEY (id),
    KEY ix_rutina_ejercicios_rutina (rutina_id, dia_numero, orden),
    CONSTRAINT fk_rutina_ejer_rutina    FOREIGN KEY (rutina_id)    REFERENCES rutinas (id) ON DELETE CASCADE,
    CONSTRAINT fk_rutina_ejer_ejercicio FOREIGN KEY (ejercicio_id) REFERENCES ejercicios (id),
    CONSTRAINT ck_rutina_ejer_dia CHECK (dia_numero BETWEEN 1 AND 7)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Plan nutricional
-- ------------------------------------------------------------
CREATE TABLE planes_nutricionales (
    id                BIGINT   NOT NULL AUTO_INCREMENT,
    usuario_id        BIGINT   NOT NULL,
    objetivo_id       BIGINT   NOT NULL,
    calorias_objetivo INT      NULL,
    activo            BOOLEAN  NOT NULL DEFAULT TRUE,
    generado_en       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY ix_planes_usuario (usuario_id, activo),
    CONSTRAINT fk_planes_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_planes_objetivo FOREIGN KEY (objetivo_id) REFERENCES objetivos (id)
) ENGINE=InnoDB;

CREATE TABLE plan_alimentos (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    plan_id     BIGINT       NOT NULL,
    alimento_id BIGINT       NOT NULL,
    momento     ENUM('DESAYUNO','ALMUERZO','CENA','SNACK') NOT NULL,
    cantidad    DECIMAL(6,2) NOT NULL,
    unidad      VARCHAR(20)  NOT NULL DEFAULT 'g',
    notas       VARCHAR(255) NULL,
    PRIMARY KEY (id),
    KEY ix_plan_alimentos_plan (plan_id, momento),
    CONSTRAINT fk_plan_alim_plan     FOREIGN KEY (plan_id)     REFERENCES planes_nutricionales (id) ON DELETE CASCADE,
    CONSTRAINT fk_plan_alim_alimento FOREIGN KEY (alimento_id) REFERENCES alimentos (id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Progreso: historial de medidas (RF10)
-- ------------------------------------------------------------
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
    CONSTRAINT fk_medidas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT ck_medidas_peso    CHECK (peso_kg > 0)
) ENGINE=InnoDB;
