<p align="center">
  <img src="img/logo_smartbody.png" alt="Logo de Smartbody" width="180">
</p>

# Smartbody

Aplicacion movil Android que genera **rutinas de entrenamiento y recomendaciones
nutricionales personalizadas** a partir de las caracteristicas fisicas, los objetivos,
la disponibilidad de tiempo y las restricciones de salud y alimentacion del usuario.

Proyecto academico de Ingenieria de Sistemas desarrollado como **Producto Minimo Viable (MVP)**
siguiendo el estandar **IEEE 830 con enfoque agil**.

---

## Arquitectura

```text
Aplicacion Android (Jetpack Compose + MVP)
              |
        Retrofit / OkHttp
        Authorization: Bearer <JWT>
              |
        API REST (Spring Boot)
              |
        MySQL / MariaDB (XAMPP + phpMyAdmin)
```

| Capa | Responsabilidad |
|------|-----------------|
| **View** (Compose) | Pantallas, formularios y estados visuales. Sin logica de negocio. |
| **Presenter** | Recibe eventos de la View, valida, llama al Model y decide que mostrar. |
| **Model** | DTO, repositorios y comunicacion con la API. |
| **Backend** | Controller → Service → Repository → MySQL, con seguridad JWT. |

---

## Estado del desarrollo

| Fase | Contenido | Estado |
|------|-----------|--------|
| 1 | Proyecto Android, backend, base de datos y comunicacion | Completada |
| 2 | Registro, BCrypt, login, JWT, interceptor, proteccion de endpoints | Completada |
| 3 | Perfil: datos personales, fisicos, objetivo, restricciones, disponibilidad | Completada |
| 4 | Ejercicios, generacion de rutina, visualizacion, detalle, sustitucion | Completada |
| 5 | Alimentos, plan nutricional y sustitucion | Completada |
| 6 | Registro de medidas, historial y grafico de progreso | Completada |
| 7 | Notificaciones (Could have) | Parcial: aviso dentro de la app para renovar la rutina a los 28 dias; sin notificaciones push |
| 8 | Pruebas y documentacion final | En curso: faltan las pruebas manuales de las pantallas nuevas en el dispositivo |

### Funcionalidades por requisito

| Requisito | Implementacion |
|-----------|----------------|
| RF01 Registro | Pantalla de registro con validaciones; contrasena con BCrypt |
| RF02 Inicio de sesion | JWT RS256 guardado en DataStore |
| RF03 Datos personales | Asistente de perfil: edad, sexo, peso, altura e IMC; foto de perfil |
| RF04 Objetivo | 5 objetivos: perder peso, ganar masa, condicion fisica, mantener peso y resistencia |
| RF05 Restricciones | Salud (lesiones, hipertension, asma...) y alimentacion (alergias, lactosa, vegano...) con aviso de salud |
| RF06 Generacion | Motor de reglas por objetivo, nivel, dias, minutos y restricciones; plan nutricional con Mifflin-St Jeor |
| RF07 Visualizacion | Rutina por dias con series, repeticiones, tiempo y descanso; plan por comidas con calorias, macronutrientes, receta de cada comida y litros de agua al dia |
| RF08 Detalle | Descripcion, grupo muscular, equipo, prescripcion e instrucciones paso a paso |
| RF09 Sustitucion | Alternativas compatibles de ejercicios y alimentos |
| RF10 Medidas | Registro de peso y medidas, grafico de evolucion e historial |
| RF11 Notificaciones | Aviso en la pantalla de inicio cuando la rutina tiene 28 dias o mas |
| RF12 Cerrar sesion | Confirmacion, eliminacion del token y regreso al login sin historial |
| Administrador | Usa todos los modulos de la app como cualquier usuario (Inicio, Rutina, Nutricion, Progreso, Perfil y chat) y ademas tiene su panel, accesible desde Inicio y Perfil: estadisticas e historial de uso, reporte de efectividad (usuarios que avanzan hacia su objetivo), detalle de cada usuario con su rutina y plan, generar un nuevo plan y activar o desactivar cuentas (sin ventas: no hay pagos en el MVP) |
| Chatbot | Boton flotante en las pantallas principales; asistente por reglas que responde con los datos del usuario (rutina, comidas, recetas, agua, progreso) y queda listo para conectar un agente de IA |

---

## Tecnologias

**Aplicacion movil**

- Kotlin, Android Studio, Jetpack Compose, Material 3
- Kotlin Coroutines, Navigation Compose
- Retrofit + OkHttp + Gson
- DataStore Preferences (almacenamiento del token)
- `minSdk 26`, `compileSdk 36`

**Backend**

- Java 17, Spring Boot 3.2.4 (Web, Data JPA, Security, Validation)
- Hibernate/JPA, MySQL Connector/J
- JJWT (RS256), BCrypt
- Maven (con wrapper incluido)

**Base de datos**

- MySQL / MariaDB administrada con XAMPP y phpMyAdmin (`smartbody_db`)

---

## Requisitos previos

- **JDK 17** (o superior compatible)
- **XAMPP** con MySQL/MariaDB
- **Android Studio** con un emulador (API 26 o superior)
- Conexion a internet la primera vez, para descargar dependencias

No es necesario instalar Maven ni Gradle: ambos proyectos incluyen su *wrapper*.

---

## Instalacion paso a paso

### 1. Base de datos (XAMPP + phpMyAdmin)

1. Abre el **panel de control de XAMPP** e inicia **MySQL**.
2. Crea la base de datos y los datos de prueba. Con phpMyAdmin: abre
   `http://localhost/phpmyadmin`, entra en la pestana **Importar** y ejecuta en orden
   `database/01_schema.sql` y luego `database/02_datos_iniciales.sql`.

   Alternativa por consola:

   ```bash
   C:\xampp\mysql\bin\mysql.exe -u root --default-character-set=utf8mb4 < database/01_schema.sql
   C:\xampp\mysql\bin\mysql.exe -u root --default-character-set=utf8mb4 < database/02_datos_iniciales.sql
   ```

   Los datos incluyen tildes y ñ: usa siempre `utf8mb4` (en phpMyAdmin, el conjunto de
   caracteres del archivo debe ser `utf-8`).

3. Verifica que exista la base de datos `smartbody_db` con sus 17 tablas.

> El script `01_schema.sql` **elimina y vuelve a crear** `smartbody_db`. Ejecutalo solo
> cuando quieras partir de cero.

### 2. Backend

```bash
cd backend
./mvnw spring-boot:run          # Linux / Git Bash
mvnw.cmd spring-boot:run        # Windows CMD / PowerShell
```

El backend queda disponible en `http://localhost:8080`.

Comprobacion rapida:

```bash
curl http://localhost:8080/api/ping
# {"servicio":"Smartbody API","estado":"ACTIVO","momento":"..."}
```

**Claves JWT.** La primera vez el backend genera un par de claves RSA en `backend/keys/`
(`private_key.pem` y `public_key.pem`). La clave privada **solo vive en el servidor** y
nunca se envia a la aplicacion Android; el directorio esta excluido del repositorio.
Para generarlas manualmente:

```bash
mkdir -p backend/keys
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out backend/keys/private_key.pem
openssl rsa -pubout -in backend/keys/private_key.pem -out backend/keys/public_key.pem
```

### 3. Aplicacion Android

1. Abre la carpeta `android/` con Android Studio (Android Studio creara `local.properties`
   con la ruta de tu SDK).
2. Inicia un emulador con API 26 o superior.
3. Ejecuta la aplicacion (`Run`), o desde consola:

   ```bash
   cd android
   ./gradlew installDebug
   ```

---

## Configuracion

Ningun secreto esta escrito en el codigo fuente. Todo se configura por variables de entorno
o propiedades externas.

### Backend (`backend/src/main/resources/application.yml`)

| Variable | Valor por defecto | Descripcion |
|----------|-------------------|-------------|
| `SMARTBODY_DB_HOST` | `localhost` | Host de MySQL |
| `SMARTBODY_DB_PORT` | `3306` | Puerto de MySQL |
| `SMARTBODY_DB_NAME` | `smartbody_db` | Nombre de la base de datos |
| `SMARTBODY_DB_USER` | `root` | Usuario de MySQL |
| `SMARTBODY_DB_PASSWORD` | *(vacio)* | Contrasena de MySQL (por defecto XAMPP no define ninguna) |
| `SMARTBODY_JWT_MINUTES` | `180` | Minutos de validez del token |
| `SMARTBODY_KEYS_DIR` | `./keys` | Directorio de las claves RSA |
| `SMARTBODY_ADMIN_EMAIL` | *(vacio)* | Correo de la cuenta de administrador que se crea al arrancar |
| `SMARTBODY_ADMIN_PASSWORD` | *(vacio)* | Contrasena del administrador (minimo 10 caracteres; solo al crearlo) |
| `SMARTBODY_ADMIN_NOMBRE` | `Administrador` | Nombre visible del administrador |

**Crear el administrador** (una sola vez; despues ya no hace falta definir las variables):

```bash
SMARTBODY_ADMIN_EMAIL=admin@smartbody.com SMARTBODY_ADMIN_PASSWORD="<contrasena segura>" ./mvnw spring-boot:run
```

En PowerShell: `$env:SMARTBODY_ADMIN_EMAIL="admin@smartbody.com"; $env:SMARTBODY_ADMIN_PASSWORD="<contrasena segura>"; .mvnw.cmd spring-boot:run`.
El administrador entra a la app con su correo como cualquier usuario (Inicio, Rutina, Nutrición, Progreso, Perfil y chat) y además ve la tarjeta **Panel de administración** en Inicio y en Perfil.

### Android

La direccion del backend esta centralizada en un unico lugar: `android/app/build.gradle.kts`,
expuesta como `BuildConfig.API_BASE_URL`.

- Emulador (valor por defecto): `http://10.0.2.2:8080/`
  `10.0.2.2` es la direccion con la que el emulador ve el `localhost` del computador.
  **No uses `localhost` desde el emulador.**
- **Celular conectado por USB (recomendado en equipos con poca RAM):**

  1. En el celular activa las *Opciones de desarrollador* y la *Depuracion USB*
     (en Xiaomi tambien *Instalar via USB*).
  2. Agrega a `android/local.properties`:

     ```properties
     smartbody.apiBaseUrl=http://127.0.0.1:8080/
     ```

  3. Con el celular conectado, redirige el puerto y luego instala la aplicacion:

     ```bash
     adb reverse tcp:8080 tcp:8080
     ./gradlew installDebug
     ```

  Hay que repetir `adb reverse` cada vez que se reconecta el cable.

- Dispositivo fisico en la misma red WiFi:

  ```bash
  ./gradlew installDebug -Psmartbody.apiBaseUrl=http://192.168.1.10:8080/
  ```

  y agrega esa IP en `app/src/main/res/xml/network_security_config.xml`.

Prioridad de la URL: parametro `-Psmartbody.apiBaseUrl` > `local.properties` > emulador.

---

## Usuario de prueba

Creado por `database/02_datos_iniciales.sql`:

| Correo | Contrasena |
|--------|------------|
| `demo@smartbody.com` | `Smartbody123` |

Es una credencial ficticia unicamente para pruebas locales.

Para los reportes del administrador, el script tambien crea 5 usuarios de prueba (`laura`, `andres`, `camila`,
`julian` y `sofia` `@prueba.smartbody.com`, contrasena `Smartbody123`) con distintos objetivos y dos meses de medidas. Tambien puedes crear una
cuenta nueva desde la pantalla de registro de la aplicacion.

---

## Estructura del proyecto

```text
SmartBody/
├── android/                     Aplicacion Android (Kotlin + Compose, patron MVP)
│   └── app/src/main/java/com/smartbody/
│       ├── model/
│       │   ├── dto/             Objetos que viajan por la API
│       │   └── repository/      Repositorios y manejo de resultados/errores
│       ├── presenter/           Logica de presentacion (login, register, dashboard, profile,
│       │                        plan, routine, nutrition, progress, splash)
│       ├── view/                Pantallas Compose, componentes, barra inferior y tema Material 3
│       ├── network/             ApiService, RetrofitClient, AuthInterceptor
│       ├── navigation/          Rutas y NavHost
│       ├── security/            TokenManager (DataStore)
│       ├── Contenedor.kt        Dependencias compartidas
│       └── MainActivity.kt
│
├── backend/                     API REST (Spring Boot)
│   └── src/main/java/com/smartbody/
│       ├── controller/          Endpoints REST
│       ├── service/             Logica de negocio
│       │   └── reglas/          Motor de reglas de rutinas y plan nutricional
│       ├── repository/          Acceso a datos (JPA)
│       ├── entity/              Entidades mapeadas a MySQL
│       ├── dto/                 Peticiones y respuestas
│       ├── security/            JWT (RS256), filtro, claves RSA
│       ├── exception/           Manejo uniforme de errores
│       └── config/              Configuracion de seguridad
│
├── database/                    Scripts SQL (esquema y datos iniciales)
└── docs/                        Documentacion tecnica
```

---

## Seguridad

- Contrasenas almacenadas con **BCrypt**; nunca en texto plano ni devueltas por la API.
- Autenticacion **JWT stateless** firmada con **RSA (RS256)**.
- La **clave privada permanece solo en el backend**; la aplicacion Android unicamente
  recibe el token.
- Todos los endpoints son privados salvo `/api/ping`, `/api/auth/register` y `/api/auth/login`.
- Validacion de entradas **en la aplicacion y en el backend** (nunca solo en el cliente).
- El token se guarda en **DataStore**, no en el codigo ni en archivos publicos.
- Trafico HTTP en claro permitido **solo** hacia el backend local de desarrollo; en un
  despliegue real debe usarse HTTPS.

---

## Pruebas

Backend:

```bash
cd backend
./mvnw test
```

85 pruebas: JWT (token valido, manipulado, expirado, firmado con otra clave), registro y
login (BCrypt, correo duplicado, credenciales incorrectas), perfil, aislamiento de datos
entre usuarios y los motores de reglas (tiempo disponible, restricciones, nivel, objetivo,
calorias, alimentos incompatibles, sustituciones, recetas, hidratacion y validacion de la foto de perfil y el chatbot).

Android:

```bash
cd android
./gradlew testDebugUnitTest
```

18 pruebas de validaciones de formularios, del formato de rutinas y medidas y del saludo del chat.

---

## Integracion y entrega continua (CI/CD)

Cada `push` a `main` ejecuta el pipeline de **GitHub Actions**
([`.github/workflows/lanzamiento.yml`](.github/workflows/lanzamiento.yml)):
Build -> Test (JUnit backend y Android) -> Package -> Publish del `jar` y el `apk` en
**GitHub Packages**. Un lanzamiento oficial se hace desde *Actions > Run workflow* con
`publicar_release`, que etiqueta la version (`vX.Y.Z`) y crea el GitHub Release listo para
desplegar con [`backend/Dockerfile`](backend/Dockerfile). Hay un `Jenkinsfile` equivalente.
Detalle en [`docs/GESTION_DE_LANZAMIENTO.md`](docs/GESTION_DE_LANZAMIENTO.md).

---

## Documentacion adicional

- [`docs/GESTION_DE_LANZAMIENTO.md`](docs/GESTION_DE_LANZAMIENTO.md) — pipeline de lanzamiento, repositorio de artefactos y release

- [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md) — capas, patron MVP, navegacion y extension a IA
- [`docs/API_ENDPOINTS.md`](docs/API_ENDPOINTS.md) — endpoints, formatos, reglas de generacion y codigos de estado
- [`docs/BASE_DE_DATOS.md`](docs/BASE_DE_DATOS.md) — modelo de datos y diagrama
- [`docs/CASOS_DE_PRUEBA.md`](docs/CASOS_DE_PRUEBA.md) — casos de prueba ejecutados
- [`docs/VERIFICACION_REQUISITOS.md`](docs/VERIFICACION_REQUISITOS.md) — cumplimiento de cada requisito del IEEE 830

---

## Aviso

Smartbody es un proyecto academico. Las rutinas y recomendaciones nutricionales que genera
**no sustituyen la valoracion de un profesional de la salud**.
