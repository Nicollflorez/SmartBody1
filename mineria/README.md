# Mineria de datos predictiva de SmartBody

Modulo academico de **regresion supervisada** que estima `semanas_estimadas`: el numero de semanas
que podria tardar un usuario en alcanzar su peso objetivo.

> **Aviso.** El dataset es **sintetico**. No es informacion clinica real. Los resultados son
> orientativos y no garantizan ninguna perdida ni ganancia de peso. El modelo es una herramienta de
> orientacion y seguimiento, no sustituye a un profesional de nutricion o salud.

---

## 1. Estructura

```text
mineria/
├── GeneradorDataset.java          Genera el dataset sintetico de 100.000 filas (CSV y ARFF)
├── sql/01_dataset_predictivo.sql  Crea smartbody_mineria.dataset_predictivo y carga el CSV
├── herramientas/
│   ├── InformeCalidadArff.java    Informe de calidad de datos (Fase 2)
│   ├── LimpiezaDataset.java       Preparacion de datos (Fase 3)
│   └── EntrenamientoModelos.java  Validacion cruzada y modelos finales (Fases 4 y 5)
├── lib/weka-stable-3.8.6.jar      Weka 3.8.6 (Maven Central: nz.ac.waikato.cms.weka:weka-stable:3.8.6)
├── datos/                         Dataset original, dataset limpio y archivo comprimido
├── informe/                       Informes de calidad, limpieza y resultados
└── modelos/                       Modelos entrenados serializados (.model)
```

Todo se ejecuta desde la carpeta `mineria/` con Java 17. No requiere Python ni Docker.

---

## 2. Reproducir el proceso

```powershell
cd c:\proyects\SmartBody\mineria

# 1. Generar el dataset sintetico (semilla 42, siempre el mismo resultado)
java GeneradorDataset.java datos

# 2. Cargar en MariaDB (base smartbody_mineria, separada de las bases de la aplicacion)
Get-Content sql\01_dataset_predictivo.sql -Raw | & "C:\xampp\mysql\bin\mysql.exe" -u root --local-infile=1

# 3. Informe de calidad del dataset original (Fase 2)
java -cp lib\weka-stable-3.8.6.jar herramientas\InformeCalidadArff.java datos\smartbody_dataset_100000.arff informe\informe_calidad_original.txt

# 4. Limpieza (Fase 3)
java -cp lib\weka-stable-3.8.6.jar herramientas\LimpiezaDataset.java datos\smartbody_dataset_100000.arff datos\smartbody_limpio.arff informe\informe_limpieza.txt

# 5. Entrenamiento y evaluacion (Fases 4 y 5)
java -Xmx2g -cp lib\weka-stable-3.8.6.jar herramientas\EntrenamientoModelos.java datos\smartbody_limpio.arff modelos informe
```

---

## 3. Comprension del negocio (Fase 1)

**Problema.** SmartBody genera planes nutricionales y rutinas, pero no estima cuanto tiempo podria
necesitar un usuario para alcanzar su peso objetivo.

**Pregunta de investigacion.** Es posible estimar el tiempo para alcanzar un peso objetivo a partir de
datos fisicos, del plan nutricional, de la actividad fisica y de la adherencia al plan?

**Objetivo de mineria de datos.** Regresion supervisada: predecir `semanas_estimadas`.

**Alcance.** Estimacion al inicio del seguimiento, usando las primeras dos semanas de adherencia. No
predice el peso dia a dia ni recomienda dietas.

**Restricciones.** Los datos son sinteticos. No hay datos longitudinales reales ni consentimiento
para usarlos.

---

## 4. Variables

### 4.1 Disponibles al inicio

| Variable (ARFF) | Tipo | Descripcion |
|---|---|---|
| `edad` | numerico | Anos (18-65 en el conjunto limpio). |
| `sexo` | nominal | `MASCULINO`, `FEMENINO`. |
| `altura_cm` | numerico | Altura en centimetros. |
| `peso_inicial_kg` | numerico | Peso al inicio. |
| `peso_objetivo_kg` | numerico | Peso que el usuario quiere alcanzar. |
| `diferencia_peso_kg` | numerico | Valor absoluto de `peso_inicial - peso_objetivo`. |
| `imc_inicial` | numerico | `peso / altura^2`. |
| `objetivo` | nominal | `PERDER_PESO`, `GANAR_MASA`. |
| `nivel_actividad` | nominal | `SEDENTARIO`, `LIGERO`, `MODERADO`, `ACTIVO`. |
| `nivel_experiencia` | nominal | `PRINCIPIANTE`, `INTERMEDIO`, `AVANZADO`. |
| `entrenamientos_semana` | numerico | Sesiones planificadas por semana (1-7). |
| `minutos_sesion` | numerico | Duracion de cada sesion (15-180). |
| `tipo_actividad` | nominal | `FUERZA`, `CARDIO`, `MIXTO`. |
| `consumo_energetico_kcal` | numerico | Gasto diario estimado (Mifflin-St Jeor x factor de actividad). |
| `balance_energetico_kcal` | numerico | Deficit diario (`PERDER_PESO`) o superavit (`GANAR_MASA`) planificado. |
| `restricciones_total` | numerico | Numero de restricciones de salud y alimentacion. |

### 4.2 Conocidas solo durante el seguimiento temprano (semanas 1-2)

| Variable | Descripcion |
|---|---|
| `adherencia_nutricional_pct` | Porcentaje del plan nutricional seguido. |
| `adherencia_ejercicio_pct` | Porcentaje de las sesiones de ejercicio completadas. |

El modelo se aplica en la semana 2, cuando ya existe esa adherencia. **No** usa el peso futuro ni
ninguna medida posterior, lo que evita la fuga de informacion.

### 4.3 Variable objetivo

| Variable | Descripcion |
|---|---|
| `semanas_estimadas` | Semanas hasta alcanzar el peso objetivo (2 a 104). |

### 4.4 Columnas de control (solo en el CSV y en la base; **no** entran al modelo)

| Columna | Uso |
|---|---|
| `id_registro` | Clave unica de cada fila. |
| `usuario_id` | Persona a la que pertenece el registro. Permite comprobar que una persona no aparezca en entrenamiento y prueba a la vez. |
| `tipo_registro` | Etiqueta de control (`LIMPIO`, `OUTLIER`, `PROBLEMATICO_*`). Permite medir la calidad de la limpieza. Sin ella, no seria posible medir la limpieza frente a un valor conocido. |

---

## 5. Generacion del dataset

`GeneradorDataset.java` crea a cada persona con reglas simples y coherentes:

- **Sexo, altura y edad** con distribuciones normales acotadas.
- **Objetivo y peso.** `PERDER_PESO` (75%) con IMC entre 21 y 38; `GANAR_MASA` (25%) con IMC entre 17,5 y 24.
  El peso objetivo siempre es coherente con el objetivo.
- **Plan.** El deficit diario se limita a 600 kcal como maximo y nunca deja la ingesta por debajo de
  1.200 kcal (mujeres) o 1.500 kcal (hombres). Asi no se recomiendan deficits extremos.
- **Adherencia.** Distribucion normal acotada entre 20% y 100%.

**Variable objetivo.** Con el deficit energetico semanal `E` (kcal) y 7.700 kcal por kilo de grasa:

```text
PERDER_PESO:  kg/semana = min( (balance*7*adh_nut + ejercicio_kcal*adh_ej) / 7700 , 1% del peso )
              ejercicio_kcal = entrenamientos * minutos * kcal_por_minuto(tipo)   (FUERZA 5, CARDIO 8, MIXTO 6,5)
GANAR_MASA:   kg/semana = 0,25 * factor_experiencia * (media de adherencias)     (1,0 / 0,7 / 0,45)

semanas_estimadas = diferencia / kg_semana * ruido,   ruido ~ N(1, 0,10) acotado a [0,75; 1,25]
                    acotado a [2, 104]
```

**Limitacion importante.** La variable objetivo es una funcion conocida de las variables de entrada,
mas ruido. Por eso las metricas de un modelo sobre estos datos seran **mejores de lo que se obtendria
con datos reales**. El resultado valida el flujo de mineria de datos, no la capacidad predictiva en
personas reales.

---

## 6. Distribucion de registros

Comprobada sobre el archivo generado (`GeneradorDataset.java` y la tabla `dataset_predictivo`):

| Tipo de registro | Filas | Porcentaje |
|---|---:|---:|
| `LIMPIO` | 80.000 | 80,00 % |
| `OUTLIER` (valores extremos plausibles) | 10.000 | 10,00 % |
| `PROBLEMATICO_DUPLICADO` | 3.500 | 3,50 % |
| `PROBLEMATICO_FALTANTE` | 3.500 | 3,50 % |
| `PROBLEMATICO_INVALIDO` | 3.000 | 3,00 % |
| **Total** | **100.000** | **100,00 %** |

Los problematicos se reparten entre duplicados (copias exactas de registros limpios), valores faltantes
(1 a 3 campos vacios) y datos invalidos (1 o 2 errores como edad -5 o 250, altura 30 o 320 cm,
adherencia 150%, minutos de 0 o 600, IMC que no coincide con peso y altura, PERDER_PESO con peso
objetivo mayor que el inicial, o semanas <= 0).

---

## 7. Fase 2: comprension de los datos

El informe completo esta en [`informe/informe_calidad_original.txt`](informe/informe_calidad_original.txt).
Resumen:

- Weka carga las 100.000 instancias y 19 atributos (18 predictores y la clase). Confirma que el ARFF es valido.
- **Faltantes:** 3.500 filas (3,50 %) con al menos un valor vacio.
- **Duplicados:** 3.500 filas repetidas (96.500 distintas).
- **Rangos y coherencia:** las reglas solo fallan en los registros invalidos. Ningun registro limpio ni
  outlier incumple una regla.
- **Valores extremos (IQR):** se observan en varias variables. Los de `restricciones_total` son un
  artefacto de los datos discretos (Q1 = 0 y Q3 = 1), no errores.

---

## 8. Fase 3: preparacion

Lo hace `herramientas/LimpiezaDataset.java`, en este orden. El informe esta en
[`informe/informe_limpieza.txt`](informe/informe_limpieza.txt).

1. **Duplicados:** se elimina cada repeticion y se conserva la primera aparicion (3.500 filas).
2. **Faltantes:** se eliminan las filas con cualquier valor vacio (3.500 filas). **No se imputa**:
   con 1 a 3 campos vacios, imputar la mediana o la moda inventaria datos y altera la relacion con la
   variable objetivo.
3. **Rangos y coherencia:** se eliminan las filas que incumplen alguna regla (3.000 filas). Todas las
   filas invalidas caen en esta regla.
4. **Outliers:** **no se eliminan**. Todos cumplen las reglas de validacion, es decir, son valores
   extremos plausibles (edades de 62 a 80 anos, pesos de 120 a 160 kg, diferencias de 25 a 45 kg), no
   errores. Eliminarlos sesgaria el modelo hacia personas tipicas.

**Resultado:** 90.000 filas limpias (`datos/smartbody_limpio.arff`).

**Transformaciones.** Los atributos nominales se codifican como Weka los necesita (no hace falta
convertirlos a mano). No se normalizan: los arboles no lo necesitan, y `LinearRegression` ya
estandariza internamente.

**Redundancia.** `diferencia_peso_kg` es una combinacion de `peso_inicial_kg` y `peso_objetivo_kg`, e
`imc_inicial` depende de peso y altura. Se conservan porque el diseno los pide, pero la regresion lineal
los trata como colineales. Esto afecta a la interpretacion de sus coeficientes, no a la prediccion.

**Separacion de datos.** Cada persona tiene un unico `usuario_id` en el conjunto limpio, porque los
duplicados ya se eliminaron. Por eso no hay personas repetidas entre entrenamiento y prueba.

---

## 9. Fases 4 y 5: modelado y evaluacion

Validacion cruzada de 10 particiones, semilla 42, 90.000 filas limpias. Los tres algoritmos ven
exactamente las mismas particiones. Fuente: [`informe/resultados_modelos.txt`](informe/resultados_modelos.txt).

| Algoritmo | MAE (sem) | RMSE (sem) | CC | R2 | Desv. MAE entre particiones | P50 error | P90 error | Tamano del modelo | Tiempo CV |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| LinearRegression | 14,112 | 18,527 | 0,6583 | 0,4333 | 0,119 | 11,132 | 29,975 | 9 KB | 20 s |
| REPTree | 4,732 | 6,957 | 0,9593 | 0,9201 | 0,033 | 3,117 | 11,064 | 1,3 MB | 78 s |
| **RandomForest** | **3,739** | **5,379** | **0,9775** | **0,9522** | 0,033 | 2,593 | 8,341 | **519 MB** | 2.916 s |

**Error por rango de semanas reales (MAE en semanas):**

| Algoritmo | 0-12 | 12-24 | 24-48 | 48-104 |
|---|---:|---:|---:|---:|
| LinearRegression | 14,46 | 13,48 | 9,41 | 22,00 |
| REPTree | 1,64 | 2,87 | 4,74 | 7,53 |
| RandomForest | 1,78 | 2,37 | 3,35 | 6,30 |

**Configuracion** (opciones de Weka, todas con semilla 42 cuando aplica):

- `LinearRegression -S 0 -R 1.0E-8 -num-decimal-places 4`
- `REPTree -M 2 -V 0.001 -N 3 -S 42 -L -1 -I 0.0`
- `RandomForest -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S 42` (100 arboles, profundidad sin limite)

### Analisis de la comparacion

- **LinearRegression no es adecuado.** La relacion entre las variables y las semanas no es lineal: el
  tiempo depende de dividir la diferencia de peso entre un ritmo que cambia con el deficit, la
  adherencia y el tipo de objetivo. Su error es alto en todos los rangos.
- **RandomForest es el mas preciso** (MAE 0,99 semanas menos que REPTree), pero tiene un costo practico
  inaceptable para SmartBody: un modelo de **519 MB** que tardaria en cargarse, ocuparia memoria del
  servicio en cada arranque y no cabe comodamente en el equipo de desarrollo (3,8 GB de RAM).
- **REPTree es el mejor equilibrio** entre error, estabilidad, interpretabilidad y tamano: pesa 1,3 MB,
  tiene la misma estabilidad que RandomForest entre particiones y entrena en 78 s. Su interpretabilidad
  es **parcial**: el arbol tiene 10.008 nodos, asi que no se puede leer completo; se explican sus ramas
  principales: el primer corte es `diferencia_peso_kg`, seguido de `objetivo`, `balance_energetico_kcal`
  y `adherencia_nutricional_pct`. No se explica el arbol entero.

**Modelo seleccionado: REPTree.** RandomForest se conserva como referencia de cota superior del error,
pero no se integra en el backend.

### Advertencias sobre estas cifras

- Las correlaciones son **altas porque la variable objetivo es una funcion de las entradas mas ruido**
  (seccion 5). Sobre personas reales el error sera mayor. Estas metricas validan el flujo, no la capacidad
  predictiva real.
- La validacion cruzada mide el error sobre datos sinteticos del mismo generador. No garantiza
  generalizacion a usuarios reales.
- El MAE de 4,7 semanas en REPTree es el error medio **dentro del mismo generador**, no una promesa de
  tiempo a una persona.

---
