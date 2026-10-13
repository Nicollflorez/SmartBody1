# Gestion de lanzamiento (Etapa 5)

Smartbody se libera con una canalizacion automatizada que compila, prueba, empaqueta y
publica el backend (`jar`) y la aplicacion Android (`apk`) sin pasos manuales propensos a
error. Toda la infraestructura corre en GitHub: el equipo de desarrollo no necesita Docker
ni servidores propios.

```text
 git push / Run workflow
          |
          v
 +-----------------+     +------------------------+     +---------------------+
 |  GitHub Actions |---->|  Build -> Test -> Pack  |---->|  GitHub Packages    |
 |  (CI/CD)        |     |  backend  |  android    |     |  jar + apk          |
 +-----------------+     +------------------------+     |  versionados        |
          |                                             +---------------------+
          | PUBLICAR_RELEASE + aprobacion                          |
          v                                                        v
 +-----------------+                                     +---------------------+
 |  Tag vX.Y.Z     |------------------------------------>|  Etapa 6: Docker /  |
 |  GitHub Release |                                     |  Kubernetes         |
 +-----------------+                                     +---------------------+
```

| Paso del plan | Herramienta elegida | Donde esta |
|---------------|---------------------|------------|
| 1. Gestion de artefactos | **GitHub Packages** (repositorio Maven del proyecto). Alternativa Nexus documentada. | `backend/pom.xml` (`distributionManagement`), `devops/maven/settings.xml`, `devops/docker-compose.yml` |
| 2. Servidor CI/CD | **GitHub Actions** (equivalente a GitLab CI/CD: YAML en el repositorio). Equivalente Jenkins incluido. | `.github/workflows/lanzamiento.yml`, `Jenkinsfile` |
| 3. Pipeline por fases | Build -> Test (JUnit) -> Package -> Publish | mismo workflow |
| 4. Transicion al despliegue | Aprobacion manual, tag `vX.Y.Z`, GitHub Release y `Dockerfile` del backend | job `release`, `backend/Dockerfile` |

---

## Paso 1. Repositorio de artefactos

Cada compilacion que pasa las pruebas deja dos artefactos **versionados** en GitHub Packages
(`https://maven.pkg.github.com/Nicollflorez/SmartBody`):

| Artefacto | Coordenadas Maven | Origen |
|-----------|-------------------|--------|
| API REST | `com.smartbody:smartbody-backend:<version>` (jar ejecutable Spring Boot) | `backend/` |
| App Android | `com.smartbody:smartbody-android:<version>` (apk) | `android/` |

- Las versiones de desarrollo llevan sufijo `-SNAPSHOT` (`1.0.0-SNAPSHOT`) y se sobreescriben
  en cada push a `main`. Las versiones oficiales (`1.0.0`) son inmutables: GitHub Packages
  rechaza volver a subir la misma version, por lo que siempre se sabe que binario exacto
  corresponde a cada lanzamiento.
- La version se define **en un solo lugar**: `backend/pom.xml`. El pipeline la propaga al
  APK (`versionName`) y usa el numero de ejecucion como `versionCode`.
- Las credenciales nunca estan en el codigo: `devops/maven/settings.xml` lee
  `GITHUB_ACTOR` y `GITHUB_TOKEN` del entorno (en Actions los provee GitHub automaticamente).

**Por que GitHub Packages y no Nexus.** El equipo desarrolla en un portatil con 4 GB de RAM;
Nexus 3 requiere ~2.7 GB de heap y Docker Desktop otros 2 GB, lo que impide compilar la app
al mismo tiempo. GitHub Packages cumple el mismo rol (repositorio Maven privado del
proyecto, versionado, con control de acceso) sin consumir recursos locales. El `pom.xml` y
el pipeline usan el protocolo estandar de `mvn deploy`, asi que migrar a Nexus en una
infraestructura propia solo requiere cambiar la URL de `distributionManagement` y la
credencial; `devops/docker-compose.yml` levanta Jenkins + Nexus para ese escenario (servidor
con 8 GB o mas).

## Paso 2. Servidor de integracion y entrega continua

El pipeline esta definido como codigo en `.github/workflows/lanzamiento.yml` y se dispara:

| Evento | Que hace |
|--------|----------|
| `pull_request` hacia `main` | Build + Test de backend y Android. No publica nada. |
| `push` a `main` | Build + Test + Package + **Publish** del SNAPSHOT en GitHub Packages. |
| `Run workflow` (manual) con `publicar_release` | Todo lo anterior con la version oficial + **Release** (tag y GitHub Release). |

Para Jenkins existe el `Jenkinsfile` equivalente (pipeline declarativo con las mismas fases y
parametros; usa la credencial `github-packages` con un token de GitHub). Se puede ejecutar
con `devops/jenkins/run-jenkins.ps1` (Jenkins nativo, sin Docker) o con
`devops/docker-compose.yml`.

## Paso 3. Fases del pipeline

| Fase | Backend (`backend/`) | Android (`android/`) | Resultado |
|------|----------------------|----------------------|-----------|
| **Version** | `mvn help:evaluate` lee la version del pom; en release se quita `-SNAPSHOT` | | `1.0.0-SNAPSHOT` o `1.0.0` |
| **Build** | `./mvnw compile` | `./gradlew assembleRelease` | Codigo compilado |
| **Test** | `./mvnw test` (85 pruebas JUnit: JWT, registro, login, motores de reglas...) | `./gradlew testDebugUnitTest` (18 pruebas) | Reporte JUnit en la pestana *Checks*; si falla, el pipeline se detiene |
| **Package** | `./mvnw package` -> `smartbody-backend-<version>.jar` | `app-release-unsigned.apk` | Artefactos adjuntos a la ejecucion |
| **Publish** | `mvn deploy:deploy-file` del jar | `mvn deploy:deploy-file` del apk | GitHub Packages |

Backend y Android se compilan **en paralelo** en dos ejecutores distintos. La fase *Publish*
sube exactamente los binarios que pasaron las pruebas (no recompila), que es el principio de
promocion de artefactos.

## Paso 4. Transicion al despliegue (Etapa 6)

1. Un responsable ejecuta **Actions > Lanzamiento Smartbody > Run workflow** con
   `publicar_release = true` (y opcionalmente `version_release` y la `api_base_url` real del
   servidor que llevara el APK).
2. El job `release` usa el entorno `produccion`. Si en *Settings > Environments* se
   configuran *Required reviewers*, GitHub detiene el pipeline hasta que alguien apruebe.
3. Al aprobarse:
   - se fija la version oficial en el pom, se crea el commit `Release vX.Y.Z` y la etiqueta
     `vX.Y.Z`;
   - se prepara la siguiente version de desarrollo (`X.Y.Z+1-SNAPSHOT`);
   - se crea el **GitHub Release** con el jar y el apk adjuntos y las notas generadas.
4. La Etapa 6 toma el jar desde GitHub Packages o desde el Release y construye la imagen con
   `backend/Dockerfile`:

   ```bash
   docker build -t smartbody-backend:1.0.0 --build-arg JAR_FILE=target/smartbody-backend-1.0.0.jar backend/
   ```

   La imagen no contiene claves ni contrasenas: todo llega por variables de entorno y el
   directorio de claves RSA se monta como volumen (`/app/keys`).

---

## Puesta en marcha

1. Crear el repositorio `Nicollflorez/SmartBody` en GitHub y subir el codigo
   (`git push -u origin main`). El primer push ya ejecuta el pipeline y publica
   `1.0.0-SNAPSHOT`.
2. (Opcional) *Settings > Environments > New environment* `produccion` > *Required reviewers*.
3. Para liberar: *Actions > Lanzamiento Smartbody > Run workflow* > marcar `publicar_release`.

No hace falta crear ningun secreto: `GITHUB_TOKEN` lo genera GitHub en cada ejecucion con los
permisos declarados en el workflow (`contents`, `packages`, `checks`).

## Evidencias sugeridas para la entrega

- Captura de la ejecucion del workflow con las fases en verde (backend y Android en paralelo).
- Pestana *Checks* con los reportes JUnit (85 + 18 pruebas).
- Pagina *Packages* del repositorio con `smartbody-backend` y `smartbody-android`.
- Aprobacion del entorno `produccion` y el *Release* `v1.0.0` con el jar y el apk adjuntos.
- Historial de commits mostrando `Release v1.0.0` y `Siguiente version de desarrollo 1.0.1-SNAPSHOT`.

## Pendientes conocidos

- El APK se publica **sin firmar** (`app-release-unsigned.apk`); para distribuirlo en
  Play Store hay que agregar un `signingConfig` con un keystore guardado como secreto del
  repositorio.
- Las pruebas instrumentadas de Android (emulador) no forman parte del pipeline; solo las
  unitarias.
