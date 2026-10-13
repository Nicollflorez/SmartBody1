# Gestion de lanzamiento (Etapa 5)

Smartbody se libera con una canalizacion automatizada que compila, prueba, empaqueta y
publica el backend (`jar`) y la aplicacion Android (`apk`) sin pasos manuales propensos a
error. Toda la infraestructura corre en la nube (gitlab.com): el equipo de desarrollo
trabaja en un portatil de 4 GB de RAM con el celular conectado por USB, sin Docker, sin
emuladores y sin servidores locales.

```text
 git push / Run pipeline
          |
          v
 +------------------+     +-------------------------+     +----------------------+
 |  GitLab CI/CD    |---->|  build -> test -> package|---->|  Package Registry    |
 |  (.gitlab-ci.yml)|     |  backend   |   android   |     |  jar + apk           |
 +------------------+     +-------------------------+     |  versionados         |
          |                                               +----------------------+
          | PUBLICAR_RELEASE + aprobacion (Play)                      |
          v                                                           v
 +------------------+                                       +----------------------+
 |  Tag vX.Y.Z      |-------------------------------------->|  Etapa 6: Docker /   |
 |  Release GitLab  |                                       |  Kubernetes          |
 +------------------+                                       +----------------------+
```

| Paso del plan | Herramienta elegida | Donde esta |
|---------------|---------------------|------------|
| 1. Gestion de artefactos | **GitLab Package Registry** (repositorio Maven del proyecto). Nexus documentado como alternativa. | `backend/pom.xml` (perfil `gitlab`), `devops/maven/settings.xml`, `devops/docker-compose.yml` |
| 2. Servidor CI/CD | **GitLab CI/CD** con `.gitlab-ci.yml` en la raiz. Equivalentes Jenkins (`Jenkinsfile`) y GitHub Actions incluidos. | `.gitlab-ci.yml`, `Jenkinsfile`, `.github/workflows/lanzamiento.yml` |
| 3. Pipeline por fases | version -> build -> test (JUnit) -> package -> publish | `.gitlab-ci.yml` |
| 4. Transicion al despliegue | Aprobacion manual, tag `vX.Y.Z`, Release de GitLab y `Dockerfile` del backend | jobs `publish:release` y `release`, `backend/Dockerfile` |

---

## Paso 1. Repositorio de artefactos

Cada pipeline que pasa las pruebas deja dos artefactos **versionados** en el Package Registry
del proyecto (*Deploy > Package Registry*):

| Artefacto | Coordenadas Maven | Origen |
|-----------|-------------------|--------|
| API REST | `com.smartbody:smartbody-backend:<version>` (jar ejecutable Spring Boot) | `backend/` |
| App Android | `com.smartbody:smartbody-android:<version>` (apk) | `android/` |

- Las versiones de desarrollo llevan sufijo `-SNAPSHOT` (`1.0.0-SNAPSHOT`) y se actualizan
  en cada push a `main`. Las versiones oficiales (`1.0.0`) se publican una sola vez y quedan
  asociadas a una etiqueta de Git, por lo que siempre se sabe que binario exacto corresponde
  a cada lanzamiento.
- La version se define **en un solo lugar**: `backend/pom.xml`. El pipeline la propaga al
  APK (`versionName`) y usa el numero de pipeline como `versionCode`.
- Las credenciales nunca estan en el codigo: `devops/maven/settings.xml` usa el token del
  job (`CI_JOB_TOKEN`) que GitLab genera en cada ejecucion.

**Por que el Package Registry y no Nexus.** El equipo desarrolla en un portatil con 4 GB de
RAM; Nexus 3 requiere ~2.7 GB de heap (y Docker Desktop otros 2 GB), lo que impide
compilar la app al mismo tiempo. El Package Registry de GitLab cumple el mismo rol
(repositorio Maven privado del proyecto, versionado, con control de acceso) sin consumir
recursos locales y con el protocolo estandar de `mvn deploy`. Migrar a Nexus en una
infraestructura propia solo requiere cambiar la URL del perfil `gitlab` del `pom.xml` y la
credencial; `devops/docker-compose.yml` levanta Jenkins + Nexus para ese escenario
(servidor con 8 GB o mas).

## Paso 2. Servidor de integracion y entrega continua

El pipeline esta definido como codigo en `.gitlab-ci.yml` y lo ejecutan los runners
compartidos de gitlab.com. Se dispara:

| Evento | Que hace |
|--------|----------|
| Merge request hacia `main` | build + test de backend y Android. No publica nada. |
| `push` a `main` | build + test + package + **publish:snapshot** en el Package Registry. |
| *Build > Pipelines > Run pipeline* con `PUBLICAR_RELEASE = true` | Todo lo anterior con la version oficial; `publish:release` espera aprobacion y luego **release** (tag + Release). |

Equivalentes incluidos en el repositorio para las otras herramientas del plan:

- `Jenkinsfile`: pipeline declarativo de Jenkins con las mismas fases y parametros
  (`devops/jenkins/run-jenkins.ps1` lo arranca sin Docker; `devops/docker-compose.yml` con
  Jenkins + Nexus).
- `.github/workflows/lanzamiento.yml`: la misma canalizacion en GitHub Actions, publicando en
  GitHub Packages (el codigo tambien vive en `github.com/Nicollflorez/SmartBody1`).

## Paso 3. Fases del pipeline

| Fase | Backend (`backend/`) | Android (`android/`) | Resultado |
|------|----------------------|----------------------|-----------|
| **version** | `mvn help:evaluate` lee la version del pom; en release se quita `-SNAPSHOT` | | `1.0.0-SNAPSHOT` o `1.0.0` (artefacto dotenv) |
| **build** | `./mvnw compile` | `./gradlew assembleRelease` | Codigo compilado y APK |
| **test** | `./mvnw test` (85 pruebas JUnit: JWT, registro, login, motores de reglas...) | `./gradlew testDebugUnitTest` (18 pruebas) | Reporte JUnit en la pestana *Tests* del pipeline; si falla, no se publica |
| **package** | `./mvnw package` -> `smartbody-backend-<version>.jar` | `app-release-unsigned.apk` | Artefactos descargables del pipeline |
| **publish** | `mvn deploy:deploy-file` del jar | `mvn deploy:deploy-file` del apk | Package Registry |

Backend y Android se ejecutan **en paralelo** en runners distintos (imagen
`eclipse-temurin:17` y `cirruslabs/android-sdk:36`). La fase *publish* sube exactamente
los binarios que pasaron las pruebas (no recompila): principio de promocion de artefactos.

## Paso 4. Transicion al despliegue (Etapa 6)

1. Un responsable ejecuta *Build > Pipelines > Run pipeline* en `main` con la variable
   `PUBLICAR_RELEASE = true` (y opcionalmente `VERSION_RELEASE` y la `API_BASE_URL` real del
   servidor que llevara el APK).
2. El pipeline compila y prueba la version oficial y se **detiene en `publish:release`**,
   un job manual: la aprobacion del lanzamiento es pulsar *Play*.
3. Al aprobarse:
   - se publican el jar y el apk oficiales en el Package Registry;
   - el job `release` crea la etiqueta `vX.Y.Z` y el **Release de GitLab** (*Deploy >
     Releases*) con enlaces a ambos artefactos. Si existe la variable `GITLAB_PUSH_TOKEN`
     (token de proyecto con `write_repository`), tambien hace el commit `Release vX.Y.Z` y
     prepara la siguiente version de desarrollo (`X.Y.Z+1-SNAPSHOT`).
4. La Etapa 6 toma el jar desde el Package Registry o el Release y construye la imagen con
   `backend/Dockerfile`:

   ```bash
   docker build -t smartbody-backend:1.0.0 --build-arg JAR_FILE=target/smartbody-backend-1.0.0.jar backend/
   ```

   La imagen no contiene claves ni contrasenas: todo llega por variables de entorno y el
   directorio de claves RSA se monta como volumen (`/app/keys`).

---

## Puesta en marcha

1. Proyecto en gitlab.com: `https://gitlab.com/Nicollflorez/smartbody` (publico). Subir el
   codigo con `git push gitlab main`; el primer push ya ejecuta el pipeline y publica el
   SNAPSHOT vigente. Los runners compartidos requieren validar la cuenta (tarjeta, sin
   cobro) la primera vez.
2. (Opcional) *Settings > CI/CD > Variables*: `GITLAB_PUSH_TOKEN` (protegido y enmascarado)
   para que el release haga el commit de version.
3. Para liberar: *Build > Pipelines > Run pipeline* > `PUBLICAR_RELEASE = true` > aprobar
   `publish:release` con *Play*.

## Evidencias sugeridas para la entrega

- Captura del pipeline con todas las fases en verde (backend y Android en paralelo).
- Pestana *Tests* del pipeline con los reportes JUnit (85 + 18 pruebas).
- *Deploy > Package Registry* con `smartbody-backend` y `smartbody-android`.
- Job `publish:release` esperando aprobacion y el *Release* `v1.0.0` con sus enlaces.

## Pendientes conocidos

- El APK se publica **sin firmar** (`app-release-unsigned.apk`); para distribuirlo en
  Play Store hay que agregar un `signingConfig` con un keystore guardado como variable
  protegida del proyecto.
- Las pruebas instrumentadas de Android (dispositivo) no forman parte del pipeline; solo las
  unitarias.
