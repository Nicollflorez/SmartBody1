# Arquitectura de microservicios

Evolucion del backend monolitico (`backend/`, entregable de la Etapa 5, que se conserva) a
cuatro microservicios en `services/`, desplegados con la misma infraestructura de las Etapas
6-8 (Docker, Kubernetes, Nginx, Prometheus/Grafana). La aplicacion Android **no cambia**: Nginx
actua como API gateway y conserva todas las rutas.

```text
                         App Android  (http://<host>/api/...)
                                   |
                      +------------+-------------+
                      |    Nginx (API gateway)   |   enruta por prefijo; bloquea /internal
                      +--+--------+--------+-----+
       /api/auth /api/users  |   /api/routines    |  /api/progress        /api/admin
       /api/ping             |   /api/nutrition   |  /api/chat
                             |   /api/objectives  |
          +---------+   +----+----+   +-----------+   +---------+
          |  auth   |   | planes  |   | progreso  |   |  admin  |
          |  :8080  |   |  :8080  |   |  :8080    |   |  :8080  |
          +----+----+   +----+----+   +-----+-----+   +----+----+
               |             |              |              |
         smartbody_auth  smartbody_planes  smartbody_progreso   (sin base de datos:
                                                                  agrega a los otros tres)
```

| Servicio | Responsabilidad | Rutas publicas | Esquema propio |
|----------|-----------------|----------------|----------------|
| **auth** | Registro, login, emision del JWT (clave privada), cuenta, perfil, foto, resumen de inicio | `/api/auth/*`, `/api/users/*`, `/api/ping` | `smartbody_auth`: usuarios, roles, usuario_roles, perfiles, usuario_restricciones, fotos_perfil, objetivos*, restricciones* |
| **planes** | Catalogos, motor de reglas, rutinas y planes nutricionales | `/api/objectives`, `/api/restrictions`, `/api/exercises/*`, `/api/routines/*`, `/api/nutrition/*` | `smartbody_planes`: objetivos*, restricciones*, ejercicios, alimentos, rutinas, rutina_ejercicios, planes_nutricionales, plan_alimentos |
| **progreso** | Medidas, evaluacion del progreso, chatbot | `/api/progress`, `/api/chat` | `smartbody_progreso`: medidas |
| **admin** | Panel de administracion: estadisticas, usuarios, efectividad, generar planes, activar/desactivar | `/api/admin/*` | ninguno (compone datos de los otros servicios) |

\* `objetivos` y `restricciones` son datos de referencia (catalogo estatico) replicados en los dos
esquemas que los usan, con los mismos identificadores. Es el patron habitual para no crear una
dependencia en tiempo de ejecucion por un catalogo que no cambia.

## Seguridad

- **Un solo emisor de tokens.** Solo `auth` tiene la clave privada RSA y firma los JWT (RS256).
  Los demas servicios reciben unicamente la **clave publica** y verifican la firma, el emisor y
  la expiracion; la identidad (`uid`, `email`, `roles`) viene en los claims, asi que no
  necesitan consultar la base de datos de usuarios.
- **Llamadas entre servicios en nombre del usuario** reenvian el mismo `Authorization: Bearer`
  de la peticion original (por ejemplo, `planes` pide el perfil a `auth` con el token del
  usuario). Asi cada servicio aplica las mismas reglas de autorizacion.
- **Llamadas internas** (las que no corresponden a un usuario, como las del panel de
  administracion sobre otros usuarios, o la sincronizacion de peso entre `auth` y `progreso`)
  usan rutas `/internal/**` protegidas por la cabecera `X-Internal-Token` (secreto compartido
  en una variable de entorno). Nginx responde `404` a `/internal/*`, de modo que solo son
  alcanzables dentro de la red del despliegue.
- Las cuentas desactivadas no pueden iniciar sesion; un token ya emitido caduca a los 180
  minutos (en el monolito se revisaba el estado en cada peticion; aqui se acepta esa ventana a
  cambio de no acoplar todos los servicios a la tabla de usuarios).

## Comunicacion entre servicios

| Origen → destino | Para que | Ruta |
|------------------|----------|------|
| planes → auth | perfil del usuario para generar rutina o plan | `GET /api/users/profile` (token del usuario) |
| auth → planes | rutina y plan activos para el resumen de inicio | `GET /api/routines/active`, `GET /api/nutrition/active` (token del usuario) |
| auth → progreso | registrar una medida cuando cambia el peso o la altura del perfil | `POST /internal/progreso/usuarios/{uid}/medidas` |
| progreso → auth | altura del perfil para el IMC y actualizar el peso tras una medida | `GET /api/users/profile` (token), `PUT /internal/usuarios/{uid}/peso` |
| progreso → auth, planes | datos para el chatbot (nombre, perfil, rutina, plan, hidratacion) | rutas publicas con el token del usuario |
| admin → auth, planes, progreso | estadisticas, listados, detalle, generar planes, activar/desactivar | `/internal/**` de cada servicio |

Los errores de negocio viajan tal cual entre servicios: un `404` de `planes` ("no tienes una
rutina activa") llega al cliente con el mismo codigo y mensaje, porque el cliente HTTP comun
convierte las respuestas `ErrorResponse` en `ApiException`.

## Libreria comun (`services/comun`)

Evita duplicar lo transversal: filtro JWT que construye el usuario autenticado desde los
claims, filtro del token interno, configuracion base de Spring Security, manejo uniforme de
errores (`ApiException`, `ErrorResponse`, `GlobalExceptionHandler`), cliente HTTP entre
servicios y el endpoint `/api/ping`.

## Despliegue

- Una imagen por servicio (`registry.gitlab.com/nicollflorez/smartbody/<servicio>`), construida
  por el pipeline con Kaniko.
- `docker-compose.microservicios.yml`: MySQL (3 esquemas), 4 servicios, Nginx gateway,
  Prometheus y Grafana.
- `k8s/microservicios/`: un Deployment + Service por microservicio, el gateway como NodePort
  30080 y el resto de la infraestructura reutilizada; `k8s/kind` del pipeline despliega esta
  variante y ejecuta la **misma coleccion end-to-end** de la Etapa 4: la aplicacion debe
  comportarse igual que el monolito.
- Prometheus raspa `/actuator/prometheus` de los cuatro servicios; el panel de Grafana agrupa
  por `application`.

## Puesta en marcha

| Escenario | Comando |
|-----------|---------|
| Servidor con Docker | `cp .env.example .env` y `docker compose -f docker-compose.microservicios.yml up -d` → API en `http://<host>/`, Grafana en `:3000` |
| Cluster Kubernetes | `VERSION=1.0.4-SNAPSHOT k8s/desplegar-microservicios.sh` → API en `:30080`, Grafana en `:30300` |
| Desarrollo local (sin Docker) | Cargar `database/microservicios/*.sql` en MySQL, `cd services && ./mvnw package`, y arrancar cada jar con `SERVER_PORT` 8081-8084, `SMARTBODY_INTERNAL_TOKEN` y `SMARTBODY_KEYS_DIR` (auth genera las claves; los demas apuntan a una carpeta con `public_key.pem`) |

Pruebas: `cd services && ./mvnw test` (88 pruebas: JWT, registro y login, perfil con llamadas a
otros servicios, foto, motores de reglas, aislamiento por usuario, progreso, chatbot, evaluador y
reporte de efectividad).

## Ejecuciones de referencia (14 de octubre de 2026)

| Que demuestra | Enlace |
|---------------|--------|
| Pipeline del Merge Request !3: compila y prueba el monolito y los cuatro servicios, construye las cinco imagenes y despliega **las dos variantes** en kind con la misma coleccion end-to-end (36 aserciones cada una) | https://gitlab.com/Nicollflorez/smartbody/-/pipelines/2946575171 |
| Suite `test:servicios` (88 pruebas) y suite `k8s:kind:microservicios` (36 aserciones) en la pestana *Tests* | https://gitlab.com/Nicollflorez/smartbody/-/pipelines/2946575171/test_report |
| Job `k8s:kind:microservicios`: los 4 pods `Running` detras del gateway y la coleccion end-to-end pasando | https://gitlab.com/Nicollflorez/smartbody/-/jobs/17169341504 |
| Imagenes `auth`, `planes`, `progreso` y `admin` | https://gitlab.com/Nicollflorez/smartbody/container_registry |

Leccion del despliegue: en un nodo de 2 vCPU la suma de *requests* de CPU (servicios + MySQL +
monitoreo + componentes del cluster) dejaba a MySQL sin programar; las reservas se redujeron a
50-100m por contenedor, que es lo que realmente consumen en reposo.

## Evolucion monolito → microservicios

| Aspecto | Monolito (`backend/`) | Microservicios (`services/`) |
|---------|------------------------|------------------------------|
| Despliegue | 1 jar / 1 imagen | 4 jars / 4 imagenes, escalables por separado |
| Base de datos | `smartbody_db` (17 tablas) | 3 esquemas con un dueno cada uno; catalogos replicados |
| Identidad | Filtro JWT que consulta la tabla de usuarios en cada peticion | `auth` emite; los demas verifican con la clave publica y leen `uid`/`roles` del token |
| Panel de administracion | Repositorios de todas las tablas | Servicio agregador sobre rutas `/internal` |
| Contrato con la app | `/api/...` | Identico: Nginx enruta por prefijo; la misma coleccion end-to-end pasa en ambos |
