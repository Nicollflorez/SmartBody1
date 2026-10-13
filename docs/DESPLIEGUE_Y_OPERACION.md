# Despliegue, operacion y monitoreo (Etapas 6, 7 y 8)

Continuacion de la [gestion de lanzamiento](GESTION_DE_LANZAMIENTO.md): el jar que publica el
pipeline se convierte en una imagen Docker, se despliega en Kubernetes detras de Nginx y se
monitorea con Prometheus y Grafana. Todo se construye y se prueba en GitLab CI; el equipo no
necesita Docker ni un cluster en su PC.

```text
                    Internet / red local
                            |
                     +-------------+   :80 (NodePort 30080)
                     |    Nginx    |   proxy inverso, gzip, limites de peticiones
                     +------+------+
                            |  /api/*
                 +----------+-----------+
                 |  backend (x2 pods)   |  Spring Boot, JWT RS256 (claves en un Secret)
                 +----------+-----------+
                            |  3306
                     +------+------+
                     |    MySQL    |  StatefulSet + disco persistente
                     +-------------+
      /actuator/prometheus ^
                           |
                +----------+----------+       +-------------+  :3000 (NodePort 30300)
                |     Prometheus      | <---- |   Grafana   |  panel "Smartbody - Backend"
                +---------------------+       +-------------+
```

| Etapa | Herramienta | Donde esta |
|-------|-------------|------------|
| 6. Desplegar | **Docker**: imagen del backend construida en el pipeline (Kaniko) y publicada en el Container Registry; `docker-compose.yml` para un servidor | `backend/Dockerfile`, `docker-compose.yml`, job `docker:backend` |
| 6. Desplegar | **Kubernetes**: manifiestos (Kustomize) con MySQL, backend, Nginx, Prometheus y Grafana; despliegue real en un cluster `kind` dentro del pipeline | `k8s/base/`, `k8s/desplegar.sh`, jobs `k8s:validate` y `k8s:kind` |
| 7. Operar | **Nginx** como proxy inverso y unico punto de entrada | `devops/nginx/`, job `nginx:validate` |
| 8. Monitor | **Prometheus** (metricas y alertas) y **Grafana** (panel) | `devops/prometheus/`, `devops/grafana/`, `k8s/base/monitoreo.yaml` |

---

## Etapa 6. Docker

### Imagen del backend

`backend/Dockerfile` parte de `eclipse-temurin:17-jre-alpine`, copia el jar **ya probado** por el
pipeline (no recompila: principio de promocion de artefactos), corre con un usuario sin
privilegios y declara un `HEALTHCHECK` sobre `/actuator/health`. Spring Boot Actuator se anadio al
backend para exponer:

- `/actuator/health/liveness` y `/actuator/health/readiness`: sondas de Docker y Kubernetes.
- `/actuator/prometheus`: metricas para la Etapa 8.

Nginx no reenvia `/actuator/*` hacia fuera (salvo `/actuator/health`), asi que estas rutas solo
son visibles dentro de la red del despliegue.

El job `docker:backend` del pipeline construye la imagen con **Kaniko** (no necesita un demonio
Docker en el runner) y la publica como:

```text
registry.gitlab.com/nicollflorez/smartbody/backend:<version>   (y :latest en main)
```

### docker-compose (un servidor)

```bash
cp .env.example .env         # cambiar contrasenas
docker compose up -d
curl http://localhost/api/ping
```

Servicios: `mysql` (carga `database/*.sql` la primera vez), `backend` (imagen del registro o
`--build` local), `nginx` (:80), `prometheus` (:9090) y `grafana` (:3000). La clave privada del
JWT vive en el volumen `jwt_keys`; MySQL y Prometheus tienen volumenes persistentes.

## Etapa 6. Kubernetes

`k8s/base/` (Kustomize) define el namespace `smartbody` y:

| Recurso | Detalle |
|---------|---------|
| `StatefulSet mysql` + `PVC` 1 Gi | Scripts SQL en el ConfigMap `smartbody-sql`; sonda `mysqladmin ping` |
| `Deployment backend` (2 replicas) | `initContainer` que espera a MySQL; sondas startup/liveness/readiness; `RollingUpdate` sin caida; limites de CPU/memoria |
| `Secret smartbody-jwt-keys` | Par de claves RSA compartido por todas las replicas (un JWT emitido por un pod vale en los demas) |
| `Deployment nginx` + `Service NodePort 30080` | Unico punto de entrada |
| `Deployment prometheus` / `grafana` + `NodePort 30300` | Monitoreo (Etapa 8) |

`k8s/desplegar.sh` crea los ConfigMaps a partir de `/database`, `/devops/nginx`,
`/devops/prometheus` y `/devops/grafana`, genera las claves RSA la primera vez, aplica los
manifiestos y espera a que todo este listo:

```bash
BACKEND_IMAGE=registry.gitlab.com/nicollflorez/smartbody/backend:1.0.3 k8s/desplegar.sh
```

Los valores de `k8s/base/secrets.yaml` son de desarrollo; en produccion se reemplazan con
`kubectl create secret` o un gestor de secretos.

### Validacion y despliegue de prueba en el pipeline

- `k8s:validate`: renderiza los manifiestos (`kubectl kustomize`) y los valida contra el esquema
  de Kubernetes 1.31 con **kubeconform**.
- `k8s:kind`: crea un cluster **kind** efimero dentro del runner (Docker-in-Docker), ejecuta
  `k8s/desplegar.sh` con la imagen recien publicada y hace una prueba de humo a traves de Nginx:
  `/api/ping` responde, `/actuator/health` esta `UP` y una ruta protegida devuelve 401.

## Etapa 7. Nginx

`devops/nginx/nginx.conf` (la misma configuracion en compose y en Kubernetes):

- Proxy inverso hacia `backend:8080` con keep-alive.
- `gzip` para las respuestas JSON.
- Limites de peticiones por IP: 5 req/s en `/api/auth/` (fuerza bruta) y 20 req/s en el resto.
- Cabeceras de seguridad, `server_tokens off`, `client_max_body_size 2m` (foto de perfil).
- `/actuator/*` bloqueado salvo `/actuator/health`.
- En produccion se termina TLS en Nginx (`listen 443 ssl` con el certificado) y la app Android
  pasa a usar `https://`.

El job `nginx:validate` comprueba la configuracion con `nginx -t` en cada pipeline.

## Etapa 8. Prometheus y Grafana

- **Prometheus** (`devops/prometheus/prometheus.yml`) raspa `/actuator/prometheus` cada 15 s.
  Reglas de alerta en `alertas.yml`: backend caido, mas del 5 % de errores 5xx, latencia p95
  mayor de 1 s y heap de la JVM por encima del 90 %.
- **Grafana** arranca con la fuente de datos y el panel **"Smartbody - Backend"** ya
  provisionados (`devops/grafana/provisioning/`): estado, peticiones por segundo, errores,
  latencia p95, peticiones y latencia por endpoint, memoria JVM y pool de conexiones a MySQL.
  Usuario `admin`; contrasena en `.env` / Secret `smartbody-grafana`.

Como no es necesario ningun cambio en la aplicacion mas alla de Actuator, las mismas metricas
serviran para cada microservicio cuando el backend se divida.

---

## Puesta en marcha rapida

| Escenario | Comando |
|-----------|---------|
| Servidor con Docker | `docker compose up -d` → API en `http://<host>/`, Grafana en `:3000` |
| Cluster Kubernetes | `k8s/desplegar.sh` → API en `http://<nodo>:30080/`, Grafana en `:30300` |
| Solo validar sin cluster | `kubectl kustomize k8s/base` + `kubeconform` (lo hace el pipeline) |

La app Android apunta al nuevo punto de entrada compilando con
`-Psmartbody.apiBaseUrl=http://<host>/` (sin el puerto 8080).

## Ejecuciones de referencia (13 de octubre de 2026)

| Que demuestra | Enlace |
|---------------|--------|
| Pipeline del Merge Request !1 con todas las etapas en verde: imagen Docker, validacion de Nginx y Kubernetes y despliegue real en kind con prueba de humo | https://gitlab.com/Nicollflorez/smartbody/-/pipelines/2944359373 |
| Job `docker:backend` (Kaniko → Container Registry) | https://gitlab.com/Nicollflorez/smartbody/-/jobs/17153852764 |
| Job `k8s:kind` (cluster efimero, `kubectl get pods`, `/api/ping` y `/actuator/health` a traves de Nginx) | https://gitlab.com/Nicollflorez/smartbody/-/jobs/17153852767 |
| Imagenes publicadas | https://gitlab.com/Nicollflorez/smartbody/container_registry |

## Pendientes conocidos

- TLS: el repositorio deja Nginx en HTTP; el certificado se agrega en el servidor real.
- El cluster `kind` del pipeline es de prueba: se crea y se destruye en cada ejecucion. Un
  despliegue permanente requiere un cluster propio (por ejemplo k3s en un VPS) y, de forma
  opcional, el Agente de Kubernetes de GitLab para desplegar desde el pipeline.
- Alertmanager (notificaciones por correo/Slack) no esta configurado; las alertas se ven en
  Prometheus y Grafana.
