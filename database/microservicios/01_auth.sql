-- ============================================================
-- Smartbody - esquema del microservicio auth
-- usuarios, roles, perfiles, foto y catalogos de referencia (objetivos y restricciones)
-- Generado a partir de database/01_schema.sql y 02_datos_iniciales.sql
-- ============================================================

DROP DATABASE IF EXISTS smartbody_auth;
CREATE DATABASE smartbody_auth
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE smartbody_auth;

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

-- ---- Datos iniciales ----
INSERT INTO roles (nombre) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

INSERT INTO objetivos (codigo, nombre, descripcion) VALUES
('PERDER_PESO',         'Perder peso',            'Reducir grasa corporal combinando fuerza y trabajo cardiovascular.'),
('GANAR_MASA',          'Ganar masa muscular',    'Aumentar volumen y fuerza muscular con cargas progresivas.'),
('MEJORAR_CONDICION',   'Mejorar condición física','Mejorar el estado físico general de forma equilibrada.'),
('MANTENER_PESO',       'Mantener peso',          'Conservar el peso actual manteniendo actividad regular.'),
('MEJORAR_RESISTENCIA', 'Mejorar resistencia',    'Aumentar la capacidad cardiovascular y la resistencia muscular.');

INSERT INTO restricciones (codigo, tipo, nombre, descripcion) VALUES
('LESION_RODILLA',   'SALUD', 'Lesión de rodilla',    'Evitar impacto y flexión profunda de rodilla.'),
('LESION_HOMBRO',    'SALUD', 'Lesión de hombro',     'Evitar empujes por encima de la cabeza.'),
('LESION_ESPALDA',   'SALUD', 'Lesión de espalda',    'Evitar carga axial y flexión de columna con peso.'),
('HIPERTENSION',     'SALUD', 'Hipertensión',         'Evitar esfuerzos máximos y ejercicios isométricos intensos.'),
('ASMA',             'SALUD', 'Asma',                 'Controlar la intensidad del trabajo cardiovascular.'),
('MOVILIDAD_REDUCIDA','SALUD','Movilidad reducida',   'Preferir ejercicios de bajo impacto y apoyo estable.'),
('INTOLERANCIA_LACTOSA','ALIMENTACION','Intolerancia a la lactosa','Evitar leche y derivados.'),
('CELIAQUIA',        'ALIMENTACION','Celiaquía / gluten','Evitar trigo, cebada, centeno y derivados.'),
('ALERGIA_FRUTOS_SECOS','ALIMENTACION','Alergia a frutos secos','Evitar nueces, almendras y similares.'),
('ALERGIA_MARISCOS', 'ALIMENTACION','Alergia a mariscos','Evitar mariscos y crustáceos.'),
('VEGETARIANO',      'ALIMENTACION','Vegetariano',     'Sin carne ni pescado.'),
('VEGANO',           'ALIMENTACION','Vegano',          'Sin ningún producto de origen animal.');

INSERT INTO usuarios (nombre, email, password_hash) VALUES
('Usuario Demo', 'demo@smartbody.com', '$2a$10$d4FMWA88lAEN5p36pI58q.M.1EfBZqyUS2sZJOOsk9RFUIIRsvoFS');

INSERT INTO usuarios (nombre, email, password_hash, creado_en)
SELECT datos.nombre, datos.email, u.password_hash, DATE_SUB(NOW(), INTERVAL 62 DAY)
FROM (
    SELECT 'Laura Gómez' AS nombre, 'laura@prueba.smartbody.com' AS email UNION ALL
    SELECT 'Andrés Rojas', 'andres@prueba.smartbody.com' UNION ALL
    SELECT 'Camila Torres', 'camila@prueba.smartbody.com' UNION ALL
    SELECT 'Julián Pérez', 'julian@prueba.smartbody.com' UNION ALL
    SELECT 'Sofía Martínez', 'sofia@prueba.smartbody.com'
) AS datos
JOIN usuarios u ON u.email = 'demo@smartbody.com';

INSERT INTO usuario_roles (usuario_id, rol_id)
SELECT u.id, r.id FROM usuarios u, roles r WHERE u.email = 'demo@smartbody.com' AND r.nombre = 'ROLE_USER';

INSERT INTO usuario_roles (usuario_id, rol_id)
SELECT u.id, r.id FROM usuarios u, roles r
WHERE u.email LIKE '%@prueba.smartbody.com' AND r.nombre = 'ROLE_USER';

INSERT INTO perfiles (usuario_id, edad, sexo, peso_kg, altura_cm, nivel_experiencia, minutos_por_sesion, dias_por_semana, objetivo_id)
SELECT u.id, 25, 'MASCULINO', 75.50, 175.00, 'PRINCIPIANTE', 45, 3, o.id
FROM usuarios u, objetivos o WHERE u.email = 'demo@smartbody.com' AND o.codigo = 'PERDER_PESO';

INSERT INTO perfiles (usuario_id, edad, sexo, peso_kg, altura_cm, nivel_experiencia, minutos_por_sesion, dias_por_semana, objetivo_id)
SELECT u.id, datos.edad, datos.sexo, datos.peso, datos.altura, datos.nivel, datos.minutos, datos.dias, o.id
FROM (
    SELECT 'laura@prueba.smartbody.com' AS email, 32 AS edad, 'FEMENINO' AS sexo, 66.5 AS peso, 162 AS altura,
           'PRINCIPIANTE' AS nivel, 45 AS minutos, 4 AS dias, 'PERDER_PESO' AS objetivo UNION ALL
    SELECT 'andres@prueba.smartbody.com', 24, 'MASCULINO', 67.2, 178, 'INTERMEDIO', 60, 5, 'GANAR_MASA' UNION ALL
    SELECT 'camila@prueba.smartbody.com', 41, 'FEMENINO', 58.6, 160, 'INTERMEDIO', 30, 3, 'MANTENER_PESO' UNION ALL
    SELECT 'julian@prueba.smartbody.com', 38, 'MASCULINO', 93.1, 175, 'PRINCIPIANTE', 30, 2, 'PERDER_PESO' UNION ALL
    SELECT 'sofia@prueba.smartbody.com', 29, 'FEMENINO', 55.4, 165, 'AVANZADO', 60, 5, 'MEJORAR_RESISTENCIA'
) AS datos
JOIN usuarios u ON u.email = datos.email
JOIN objetivos o ON o.codigo = datos.objetivo;

INSERT INTO usuario_restricciones (usuario_id, restriccion_id)
SELECT u.id, r.id FROM usuarios u, restricciones r
WHERE u.email = 'demo@smartbody.com' AND r.codigo IN ('LESION_RODILLA','INTOLERANCIA_LACTOSA');

INSERT INTO usuario_restricciones (usuario_id, restriccion_id)
SELECT u.id, r.id FROM usuarios u JOIN restricciones r
  ON (u.email = 'laura@prueba.smartbody.com'  AND r.codigo = 'INTOLERANCIA_LACTOSA')
  OR (u.email = 'julian@prueba.smartbody.com' AND r.codigo IN ('HIPERTENSION', 'LESION_RODILLA'))
  OR (u.email = 'sofia@prueba.smartbody.com'  AND r.codigo = 'VEGETARIANO');

