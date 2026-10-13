-- ============================================================
-- Smartbody - Datos iniciales de prueba
-- Ejecutar despues de 01_schema.sql
-- Usuario de prueba: demo@smartbody.com / Smartbody123  (contrasena de prueba, no usar datos reales)
-- ============================================================

USE smartbody_db;

-- ------------------------------------------------------------
-- Roles
-- ------------------------------------------------------------
INSERT INTO roles (nombre) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

-- ------------------------------------------------------------
-- Objetivos (RF04)
-- ------------------------------------------------------------
INSERT INTO objetivos (codigo, nombre, descripcion) VALUES
('PERDER_PESO',         'Perder peso',            'Reducir grasa corporal combinando fuerza y trabajo cardiovascular.'),
('GANAR_MASA',          'Ganar masa muscular',    'Aumentar volumen y fuerza muscular con cargas progresivas.'),
('MEJORAR_CONDICION',   'Mejorar condición física','Mejorar el estado físico general de forma equilibrada.'),
('MANTENER_PESO',       'Mantener peso',          'Conservar el peso actual manteniendo actividad regular.'),
('MEJORAR_RESISTENCIA', 'Mejorar resistencia',    'Aumentar la capacidad cardiovascular y la resistencia muscular.');

-- ------------------------------------------------------------
-- Restricciones (RF05)
-- ------------------------------------------------------------
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

-- ------------------------------------------------------------
-- Ejercicios (RF06, RF08)
-- ------------------------------------------------------------
INSERT INTO ejercicios (nombre, descripcion, grupo_muscular, equipamiento, tipo, nivel_minimo, duracion_estimada_seg, instrucciones) VALUES
('Sentadilla libre',      'Ejercicio básico de piernas con peso corporal o barra.', 'PIERNAS', 'NINGUNO',   'FUERZA', 'PRINCIPIANTE', 240, 'Pies al ancho de hombros. Baja controlando la espalda recta hasta que los muslos queden paralelos al suelo y sube empujando con los talones.'),
('Prensa de piernas',     'Empuje de piernas en máquina, muy estable.',            'PIERNAS', 'MAQUINA',   'FUERZA', 'PRINCIPIANTE', 240, 'Apoya toda la espalda en el respaldo, baja hasta 90 grados y empuja sin bloquear las rodillas.'),
('Zancadas',              'Trabajo unilateral de piernas y glúteo.',               'PIERNAS', 'NINGUNO',   'FUERZA', 'INTERMEDIO',   240, 'Da un paso al frente y baja la rodilla trasera sin tocar el suelo. Alterna piernas.'),
('Peso muerto rumano',    'Trabajo de cadena posterior con bisagra de cadera.',     'PIERNAS', 'MANCUERNAS','FUERZA', 'INTERMEDIO',   270, 'Lleva la cadera atrás manteniendo la espalda recta y las rodillas semiflexionadas.'),
('Puente de glúteos',     'Activación de glúteos de bajo impacto.',                'PIERNAS', 'NINGUNO',   'FUERZA', 'PRINCIPIANTE', 180, 'Boca arriba con rodillas flexionadas, eleva la cadera apretando los glúteos.'),
('Extensión de cuádriceps','Aislamiento de cuádriceps en máquina.',                'PIERNAS', 'MAQUINA',   'FUERZA', 'PRINCIPIANTE', 180, 'Extiende las rodillas de forma controlada y baja lentamente.'),
('Flexiones de pecho',    'Empuje horizontal con peso corporal.',                  'PECHO',   'NINGUNO',   'FUERZA', 'PRINCIPIANTE', 210, 'Manos al ancho de hombros, cuerpo alineado, baja hasta que el pecho quede cerca del suelo.'),
('Press banca',           'Empuje horizontal con barra.',                          'PECHO',   'BARRA',     'FUERZA', 'INTERMEDIO',   270, 'Baja la barra al pecho de forma controlada y empuja sin arquear en exceso la espalda.'),
('Press inclinado mancuernas','Empuje en banco inclinado.',                        'PECHO',   'MANCUERNAS','FUERZA', 'INTERMEDIO',   240, 'Banco a 30 grados, baja las mancuernas a la altura del pecho y empuja hacia arriba.'),
('Aperturas en máquina',  'Aislamiento de pectoral.',                              'PECHO',   'MAQUINA',   'FUERZA', 'PRINCIPIANTE', 180, 'Junta los brazos al frente con codos ligeramente flexionados.'),
('Remo con mancuerna',    'Tracción horizontal para espalda.',                     'ESPALDA', 'MANCUERNAS','FUERZA', 'PRINCIPIANTE', 240, 'Apoya una mano en el banco, tira de la mancuerna hacia la cadera con la espalda recta.'),
('Jalón al pecho',        'Tracción vertical en polea.',                           'ESPALDA', 'MAQUINA',   'FUERZA', 'PRINCIPIANTE', 210, 'Tira de la barra hacia el pecho llevando los codos abajo y atrás.'),
('Dominadas asistidas',   'Tracción vertical con asistencia.',                     'ESPALDA', 'MAQUINA',   'FUERZA', 'INTERMEDIO',   240, 'Sube hasta que la barbilla supere la barra y baja controlando.'),
('Remo en polea baja',    'Tracción horizontal sentado.',                          'ESPALDA', 'MAQUINA',   'FUERZA', 'PRINCIPIANTE', 210, 'Mantén el torso firme y lleva el agarre al abdomen juntando las escápulas.'),
('Press militar',         'Empuje vertical de hombros.',                           'HOMBROS', 'BARRA',     'FUERZA', 'INTERMEDIO',   240, 'Empuja la barra por encima de la cabeza sin arquear la espalda.'),
('Elevaciones laterales', 'Aislamiento de deltoides medio.',                       'HOMBROS', 'MANCUERNAS','FUERZA', 'PRINCIPIANTE', 180, 'Eleva los brazos a los lados hasta la altura de los hombros.'),
('Pájaros posteriores',   'Trabajo de deltoides posterior.',                       'HOMBROS', 'MANCUERNAS','FUERZA', 'PRINCIPIANTE', 180, 'Inclina el torso al frente y abre los brazos a los lados.'),
('Curl de bíceps',        'Flexión de codo con mancuernas.',                       'BRAZOS',  'MANCUERNAS','FUERZA', 'PRINCIPIANTE', 180, 'Flexiona los codos sin balancear el cuerpo y baja controlando.'),
('Extensión de tríceps en polea','Aislamiento de tríceps.',                        'BRAZOS',  'MAQUINA',   'FUERZA', 'PRINCIPIANTE', 180, 'Codos pegados al cuerpo, extiende los brazos hacia abajo.'),
('Fondos en banco',       'Trabajo de tríceps con peso corporal.',                 'BRAZOS',  'NINGUNO',   'FUERZA', 'PRINCIPIANTE', 180, 'Apoya las manos en un banco detrás del cuerpo y baja flexionando los codos.'),
('Plancha abdominal',     'Isométrico de core.',                                   'CORE',    'NINGUNO',   'FUERZA', 'PRINCIPIANTE', 120, 'Apoya antebrazos y puntas de los pies manteniendo el cuerpo alineado.'),
('Crunch abdominal',      'Flexión corta de tronco.',                              'CORE',    'NINGUNO',   'FUERZA', 'PRINCIPIANTE', 150, 'Eleva los hombros del suelo sin tirar del cuello.'),
('Elevación de piernas',  'Trabajo de abdomen inferior.',                          'CORE',    'NINGUNO',   'FUERZA', 'INTERMEDIO',   150, 'Boca arriba, sube las piernas rectas y baja sin tocar el suelo.'),
('Plancha lateral',       'Isométrico de oblicuos.',                               'CORE',    'NINGUNO',   'FUERZA', 'PRINCIPIANTE', 120, 'Apoya un antebrazo y el canto del pie manteniendo la cadera elevada.'),
('Burpees',               'Ejercicio de cuerpo completo de alta intensidad.',      'CUERPO_COMPLETO','NINGUNO','CARDIO','INTERMEDIO', 180, 'De pie, baja a plancha, haz una flexión y salta hacia arriba.'),
('Sentadilla con salto',  'Pliometría de piernas.',                               'PIERNAS', 'NINGUNO',   'CARDIO', 'INTERMEDIO',   150, 'Realiza una sentadilla y sube con un salto amortiguando la caida.'),
('Caminata rápida',       'Cardio de bajo impacto.',                              'CARDIO',  'NINGUNO',   'CARDIO', 'PRINCIPIANTE', 600, 'Camina a ritmo sostenido manteniendo una respiración controlada.'),
('Bicicleta estática',    'Cardio de bajo impacto en máquina.',                   'CARDIO',  'MAQUINA',   'CARDIO', 'PRINCIPIANTE', 600, 'Pedalea a ritmo constante ajustando la resistencia.'),
('Elíptica',              'Cardio continuo sin impacto articular.',               'CARDIO',  'MAQUINA',   'CARDIO', 'PRINCIPIANTE', 600, 'Mantén un ritmo constante usando brazos y piernas.'),
('Cuerda / saltos',       'Cardio de alta intensidad.',                           'CARDIO',  'NINGUNO',   'CARDIO', 'INTERMEDIO',   300, 'Salta manteniendo un ritmo constante y aterrizando con las rodillas suaves.'),
('Movilidad de cadera',   'Rutina corta de movilidad articular.',                 'MOVILIDAD','NINGUNO',  'MOVILIDAD','PRINCIPIANTE',180,'Realiza circulos y aperturas de cadera de forma lenta y controlada.'),
('Estiramiento general',  'Estiramientos de las principales cadenas musculares.', 'MOVILIDAD','NINGUNO',  'MOVILIDAD','PRINCIPIANTE',180,'Mantén cada estiramiento 20-30 segundos sin rebotes.');

-- Ejercicios contraindicados segun restriccion de salud
INSERT INTO ejercicio_restricciones (ejercicio_id, restriccion_id)
SELECT e.id, r.id FROM ejercicios e, restricciones r
WHERE (r.codigo = 'LESION_RODILLA'    AND e.nombre IN ('Sentadilla libre','Zancadas','Sentadilla con salto','Burpees','Cuerda / saltos','Extensión de cuádriceps'))
   OR (r.codigo = 'LESION_HOMBRO'     AND e.nombre IN ('Press militar','Press banca','Dominadas asistidas','Flexiones de pecho','Elevaciones laterales','Burpees'))
   OR (r.codigo = 'LESION_ESPALDA'    AND e.nombre IN ('Peso muerto rumano','Remo con mancuerna','Sentadilla libre','Press militar','Burpees'))
   OR (r.codigo = 'HIPERTENSION'      AND e.nombre IN ('Burpees','Sentadilla con salto','Cuerda / saltos','Plancha abdominal','Plancha lateral'))
   OR (r.codigo = 'ASMA'              AND e.nombre IN ('Burpees','Cuerda / saltos','Sentadilla con salto'))
   OR (r.codigo = 'MOVILIDAD_REDUCIDA'AND e.nombre IN ('Burpees','Sentadilla con salto','Cuerda / saltos','Zancadas','Dominadas asistidas','Elevación de piernas'));

-- ------------------------------------------------------------
-- Alimentos (plan nutricional)
-- ------------------------------------------------------------
INSERT INTO alimentos (nombre, grupo, calorias_100g, proteina_g, carbohidratos_g, grasas_g, descripcion, preparacion, icono) VALUES
('Pechuga de pollo',   'PROTEINA',   165, 31.0,  0.0,  3.6, 'Fuente magra de proteína animal.',
    'Sazona la pechuga con sal, pimienta y ajo, y cocínala a la plancha de 6 a 7 minutos por cada lado.', '🍗'),
('Carne de res magra', 'PROTEINA',   187, 26.0,  0.0,  9.0, 'Aporta proteína y hierro.',
    'Sazona la carne y cocínala en una sartén bien caliente de 4 a 5 minutos por cada lado.', '🥩'),
('Huevo',              'PROTEINA',   155, 13.0,  1.1, 11.0, 'Proteína completa y económica.',
    'Prepara los huevos revueltos en sartén antiadherente o cocidos durante 10 minutos en agua hirviendo.', '🥚'),
('Salmón',             'PROTEINA',   208, 20.0,  0.0, 13.0, 'Rico en omega 3.',
    'Hornea el salmón a 200 °C de 12 a 15 minutos con limón, sal y hierbas.', '🐟'),
('Atún en agua',       'PROTEINA',   116, 26.0,  0.0,  1.0, 'Práctico y alto en proteína.',
    'Escurre el atún y mézclalo con limón y cebolla picada.', '🥫'),
('Camarón',            'PROTEINA',    99, 24.0,  0.2,  0.3, 'Marisco bajo en grasa.',
    'Saltea los camarones con ajo en una sartén caliente de 3 a 4 minutos, hasta que cambien de color.', '🦐'),
('Lentejas',           'PROTEINA',   116,  9.0, 20.0,  0.4, 'Proteína vegetal con fibra.',
    'Cocina las lentejas con cebolla, ajo y zanahoria durante 25 minutos a fuego medio.', '🍲'),
('Frijoles',           'PROTEINA',   127,  8.7, 22.8,  0.5, 'Legumbre rica en fibra.',
    'Calienta los frijoles cocidos con cebolla y comino. Si son secos, déjalos en remojo la noche anterior.', '🫘'),
('Garbanzos',          'PROTEINA',   164,  8.9, 27.4,  2.6, 'Legumbre saciante.',
    'Escurre los garbanzos cocidos y saltéalos con ajo y paprika durante 5 minutos.', '🧆'),
('Tofu',               'PROTEINA',   144, 15.0,  3.0,  9.0, 'Proteína vegetal a base de soya.',
    'Corta el tofu en cubos, sécalo con papel y dóralo en sartén con salsa de soya.', '🥢'),
('Yogur griego',       'LACTEO',      59, 10.0,  3.6,  0.4, 'Alto en proteína, contiene lactosa.',
    'Sirve el yogur frío y sin azúcar añadida.', '🥣'),
('Leche descremada',   'LACTEO',      35,  3.4,  5.0,  0.1, 'Fuente de calcio, contiene lactosa.',
    'Sirve la leche fría o tibia, sin azúcar añadida.', '🥛'),
('Queso fresco',       'LACTEO',     264, 18.0,  3.0, 20.0, 'Lácteo con grasa moderada.',
    'Corta el queso en tajadas o cubos.', '🧀'),
('Arroz integral',     'CARBOHIDRATO',111, 2.6, 23.0,  0.9, 'Carbohidrato complejo con fibra.',
    'Cocina el arroz en el doble de agua de 35 a 40 minutos a fuego bajo y tapado.', '🍚'),
('Avena',              'CARBOHIDRATO',389,16.9, 66.3,  6.9, 'Cereal de digestión lenta, puede contener gluten.',
    'Cocina la avena con agua o leche durante 5 minutos, revolviendo constantemente.', '🌾'),
('Pan integral',       'CARBOHIDRATO',247,13.0, 41.0,  3.4, 'Contiene gluten.',
    'Tuesta el pan integral en sartén o tostadora.', '🍞'),
('Pasta integral',     'CARBOHIDRATO',124, 5.3, 25.0,  0.5, 'Contiene gluten.',
    'Cocina la pasta en agua hirviendo con sal de 10 a 12 minutos y escúrrela.', '🍝'),
('Papa',               'CARBOHIDRATO', 77, 2.0, 17.0,  0.1, 'Carbohidrato natural sin gluten.',
    'Cocina la papa con cáscara en agua durante 20 minutos o ásala en el horno.', '🥔'),
('Batata',             'CARBOHIDRATO', 86, 1.6, 20.1,  0.1, 'Carbohidrato con bajo índice glucémico.',
    'Hornea la batata a 200 °C durante 30 minutos o cocínala en agua durante 20 minutos.', '🍠'),
('Quinua',             'CARBOHIDRATO',120, 4.4, 21.3,  1.9, 'Cereal sin gluten con proteína completa.',
    'Lava bien la quinua y cocínala en el doble de agua durante 15 minutos.', '🌱'),
('Plátano',            'FRUTA',        89, 1.1, 22.8,  0.3, 'Fruta energética rica en potasio.',
    'Pela el plátano y córtalo en rodajas.', '🍌'),
('Manzana',            'FRUTA',        52, 0.3, 13.8,  0.2, 'Fruta con fibra y baja en calorías.',
    'Lava la manzana y córtala en trozos.', '🍎'),
('Fresas',             'FRUTA',        32, 0.7,  7.7,  0.3, 'Fruta baja en calorías y antioxidante.',
    'Lava las fresas y retira las hojas.', '🍓'),
('Naranja',            'FRUTA',        47, 0.9, 11.8,  0.1, 'Fuente de vitamina C.',
    'Pela la naranja y sepárala en gajos.', '🍊'),
('Brócoli',            'VERDURA',      34, 2.8,  6.6,  0.4, 'Verdura rica en fibra y micronutrientes.',
    'Cocina el brócoli al vapor durante 5 minutos para que quede crujiente.', '🥦'),
('Espinaca',           'VERDURA',      23, 2.9,  3.6,  0.4, 'Verdura de hoja verde rica en hierro.',
    'Saltea la espinaca durante 2 minutos o úsala fresca en ensalada.', '🥬'),
('Zanahoria',          'VERDURA',      41, 0.9,  9.6,  0.2, 'Verdura rica en betacarotenos.',
    'Ralla la zanahoria o córtala en bastones y cocínala al vapor durante 8 minutos.', '🥕'),
('Ensalada mixta',     'VERDURA',      25, 1.5,  4.0,  0.2, 'Mezcla de hojas verdes y vegetales frescos.',
    'Lava y corta las hojas y los vegetales, y alíñalos con limón.', '🥗'),
('Aguacate',           'GRASA',       160, 2.0,  8.5, 14.7, 'Grasa saludable monoinsaturada.',
    'Corta el aguacate en tajadas justo antes de servir.', '🥑'),
('Aceite de oliva',    'GRASA',       884, 0.0,  0.0,100.0, 'Grasa saludable para cocinar o aderezar.',
    'Agrega el aceite en crudo al final para aliñar.', '🫒'),
('Almendras',          'GRASA',       579,21.2, 21.6, 49.9, 'Fruto seco con grasas saludables.',
    'Sírvelas naturales, sin sal ni azúcar.', '🌰'),
('Mantequilla de maní','GRASA',       588,25.0, 20.0, 50.0, 'Fuente de grasa y proteína vegetal.',
    'Úsala natural, sin azúcar añadida, untada o acompañando la fruta.', '🥜');

-- Momentos del dia recomendados para cada alimento (NULL: cualquier comida).
-- Evita, por ejemplo, avena o yogur en el almuerzo y aceite de oliva en el snack.
UPDATE alimentos SET momentos = 'DESAYUNO,SNACK'
WHERE nombre IN ('Avena', 'Pan integral', 'Yogur griego', 'Leche descremada', 'Queso fresco',
                 'Plátano', 'Manzana', 'Fresas', 'Naranja', 'Mantequilla de maní');
UPDATE alimentos SET momentos = 'ALMUERZO,CENA'
WHERE nombre IN ('Pechuga de pollo', 'Carne de res magra', 'Salmón', 'Atún en agua', 'Camarón',
                 'Lentejas', 'Frijoles', 'Garbanzos', 'Arroz integral', 'Pasta integral', 'Batata',
                 'Brócoli', 'Espinaca', 'Zanahoria', 'Ensalada mixta', 'Aceite de oliva');

-- Alimentos incompatibles segun restriccion alimentaria
INSERT INTO alimento_restricciones (alimento_id, restriccion_id)
SELECT a.id, r.id FROM alimentos a, restricciones r
WHERE (r.codigo = 'INTOLERANCIA_LACTOSA' AND a.grupo = 'LACTEO')
   OR (r.codigo = 'CELIAQUIA'            AND a.nombre IN ('Pan integral','Pasta integral','Avena'))
   OR (r.codigo = 'ALERGIA_FRUTOS_SECOS' AND a.nombre IN ('Almendras','Mantequilla de maní'))
   OR (r.codigo = 'ALERGIA_MARISCOS'     AND a.nombre IN ('Camarón'))
   OR (r.codigo = 'VEGETARIANO'          AND a.nombre IN ('Pechuga de pollo','Carne de res magra','Salmón','Atún en agua','Camarón'))
   OR (r.codigo = 'VEGANO'               AND (a.grupo = 'LACTEO' OR a.nombre IN ('Pechuga de pollo','Carne de res magra','Salmón','Atún en agua','Camarón','Huevo')));

-- ------------------------------------------------------------
-- Usuario de prueba (contrasena de prueba: Smartbody123)
-- ------------------------------------------------------------
INSERT INTO usuarios (nombre, email, password_hash) VALUES
('Usuario Demo', 'demo@smartbody.com', '$2a$10$d4FMWA88lAEN5p36pI58q.M.1EfBZqyUS2sZJOOsk9RFUIIRsvoFS');

INSERT INTO usuario_roles (usuario_id, rol_id)
SELECT u.id, r.id FROM usuarios u, roles r WHERE u.email = 'demo@smartbody.com' AND r.nombre = 'ROLE_USER';

INSERT INTO perfiles (usuario_id, edad, sexo, peso_kg, altura_cm, nivel_experiencia, minutos_por_sesion, dias_por_semana, objetivo_id)
SELECT u.id, 25, 'MASCULINO', 75.50, 175.00, 'PRINCIPIANTE', 45, 3, o.id
FROM usuarios u, objetivos o WHERE u.email = 'demo@smartbody.com' AND o.codigo = 'PERDER_PESO';

INSERT INTO usuario_restricciones (usuario_id, restriccion_id)
SELECT u.id, r.id FROM usuarios u, restricciones r
WHERE u.email = 'demo@smartbody.com' AND r.codigo IN ('LESION_RODILLA','INTOLERANCIA_LACTOSA');

INSERT INTO medidas (usuario_id, peso_kg, altura_cm, cintura_cm, imc, registrado_en)
SELECT u.id, 78.00, 175.00, 92.00, 25.47, DATE_SUB(NOW(), INTERVAL 30 DAY) FROM usuarios u WHERE u.email = 'demo@smartbody.com'
UNION ALL
SELECT u.id, 76.50, 175.00, 90.00, 24.98, DATE_SUB(NOW(), INTERVAL 15 DAY) FROM usuarios u WHERE u.email = 'demo@smartbody.com'
UNION ALL
SELECT u.id, 75.50, 175.00, 88.50, 24.65, NOW() FROM usuarios u WHERE u.email = 'demo@smartbody.com';

-- ------------------------------------------------------------
-- Usuarios de prueba para los reportes del administrador (contrasena de prueba: Smartbody123)
-- Datos ficticios con distintos objetivos e historial de medidas de 2 meses.
-- ------------------------------------------------------------
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
SELECT u.id, r.id FROM usuarios u, roles r
WHERE u.email LIKE '%@prueba.smartbody.com' AND r.nombre = 'ROLE_USER';

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
SELECT u.id, r.id FROM usuarios u JOIN restricciones r
  ON (u.email = 'laura@prueba.smartbody.com'  AND r.codigo = 'INTOLERANCIA_LACTOSA')
  OR (u.email = 'julian@prueba.smartbody.com' AND r.codigo IN ('HIPERTENSION', 'LESION_RODILLA'))
  OR (u.email = 'sofia@prueba.smartbody.com'  AND r.codigo = 'VEGETARIANO');

-- Historial de peso: una medida cada 20 dias. IMC = peso / (altura en metros)^2.
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
JOIN usuarios u ON u.email = datos.email;
