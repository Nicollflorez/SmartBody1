# Planificacion en Trello (Etapa 1)

Tablero **Smartbody** en Trello con el ciclo de vida completo del proyecto. Esta pagina es la
guia para crearlo: las listas, las tarjetas (con etiquetas y lista de verificacion) y como
enlazarlas con GitLab. Cuando el tablero exista, enlazarlo aqui:

> Tablero: `https://trello.com/b/...` (pendiente)

## Listas (columnas)

| Lista | Uso |
|-------|-----|
| **Backlog** | Todo lo identificado y aun no planificado |
| **Por hacer (sprint)** | Comprometido para la iteracion actual |
| **En proceso** | Alguien esta trabajando en ello |
| **En revision** | Merge Request abierto / pruebas en el pipeline |
| **Hecho** | Integrado en `main` con el pipeline en verde |

## Etiquetas

| Etiqueta | Color | Significado |
|----------|-------|-------------|
| `RF` | verde | Requisito funcional (IEEE 830) |
| `Android` | azul | Aplicacion movil |
| `Backend` | naranja | API REST |
| `DevOps` | morado | Pipeline, despliegue, operacion, monitoreo |
| `Docs` | amarillo | Documentacion y evidencias |
| `Must` / `Should` / `Could` | rojo / naranja / gris | Prioridad MoSCoW |

## Tarjetas

Cada tarjeta lleva: descripcion (una frase), etiquetas, lista de verificacion y, cuando aplica,
el enlace al Merge Request o pipeline de GitLab que la cerro.

### Hecho — requisitos del MVP (fases 1 a 7 del README)

| Tarjeta | Etiquetas | Lista de verificacion |
|---------|-----------|-----------------------|
| RF01 Registro de usuario | RF, Android, Backend, Must | pantalla, validaciones, BCrypt, prueba JUnit, caso e2e |
| RF02 Inicio de sesion con JWT | RF, Android, Backend, Must | login, token RS256, interceptor, DataStore, caso e2e |
| RF03 Datos personales y foto | RF, Android, Backend, Must | asistente de perfil, IMC, foto (<700 KB) |
| RF04 Objetivo de entrenamiento | RF, Backend, Must | catalogo de 5 objetivos |
| RF05 Restricciones de salud y alimentacion | RF, Backend, Must | catalogo, aviso de salud |
| RF06 Generacion de rutina y plan nutricional | RF, Backend, Must | motor de reglas, Mifflin-St Jeor, pruebas |
| RF07 Visualizacion de rutina y plan | RF, Android, Must | rutina por dias, comidas, recetas, agua |
| RF08 Detalle de ejercicio | RF, Android, Backend, Should | descripcion, instrucciones |
| RF09 Sustitucion de ejercicios y alimentos | RF, Backend, Should | alternativas compatibles |
| RF10 Registro de medidas y progreso | RF, Android, Backend, Must | historial, grafico, caso e2e |
| RF11 Aviso de renovacion de rutina | RF, Android, Could | aviso a los 28 dias |
| RF12 Cerrar sesion | RF, Android, Must | confirmacion, borrado del token |
| Panel de administracion | Backend, Android, Should | estadisticas, efectividad, activar/desactivar cuentas |
| Chatbot por reglas | Backend, Android, Could | intenciones, datos del usuario, 403/401 |

### Hecho — ciclo de vida DevOps (etapas 2 a 8 del documento guia)

| Tarjeta | Etiquetas | Enlace |
|---------|-----------|--------|
| Etapa 2-3: VS Code, Maven y Gradle con wrappers | DevOps | README |
| Etapa 4: pruebas JUnit (85 backend + 22 Android) | DevOps | pestana *Tests* del pipeline |
| Etapa 4: pruebas end-to-end de la API con Postman/Newman | DevOps | MR !2 |
| Etapa 5: pipeline GitLab CI/CD (version → build → test → package → publish → release) | DevOps | `docs/GESTION_DE_LANZAMIENTO.md` |
| Etapa 5: Package Registry y releases v1.0.1-v1.0.3 | DevOps | *Deploy > Releases* |
| Etapa 5: equivalentes Jenkins y GitHub Actions | DevOps, Docs | `Jenkinsfile`, `.github/workflows/` |
| Etapa 6: imagen Docker (Kaniko) y docker-compose | DevOps | MR !1 |
| Etapa 6: manifiestos Kubernetes y despliegue en kind | DevOps | MR !1, job `k8s:kind` |
| Etapa 7: Nginx como proxy inverso | DevOps | MR !1 |
| Etapa 8: Prometheus + Grafana | DevOps | MR !1 |

### Por hacer — siguientes iteraciones

| Tarjeta | Etiquetas | Lista de verificacion |
|---------|-----------|-----------------------|
| Etapa 1: tablero Trello enlazado desde el README | Docs | crear tablero, enlazar, capturas |
| Firmar el APK de release con un keystore en variables protegidas | DevOps, Android | keystore, `signingConfig`, variable enmascarada |
| TLS en Nginx (certificado real) | DevOps | `listen 443 ssl`, redireccion 80 → 443, app con `https://` |
| Alertmanager con notificaciones | DevOps | correo o Slack para las 4 alertas |
| Microservicio `auth` (registro, login, JWT, perfil, foto) | Backend, DevOps | modulo, esquema propio, imagen, manifiestos, pruebas |
| Microservicio `planes` (catalogo, rutinas, nutricion, motores de reglas) | Backend, DevOps | idem |
| Microservicio `progreso` (medidas, evaluador, chatbot) | Backend, DevOps | idem |
| Microservicio `admin` (reportes, estado de cuentas, planes de otros usuarios) | Backend, DevOps | idem |
| API gateway en Nginx para los 4 servicios | DevOps | rutas por prefijo, clave publica compartida |
| Pruebas instrumentadas Android (Espresso) en el pipeline | Android, DevOps | emulador headless en el runner |
| Notificaciones push (RF11 completo) | Android, Backend, Could | FCM |

## Integracion con GitLab

- Cada tarjeta en *En revision* enlaza su Merge Request; el Power-Up **GitLab** de Trello (o el
  enlace manual) muestra el estado del pipeline en la tarjeta.
- Convencion de ramas: `etapa-<n>-<tema>` (por ejemplo `etapa-6-7-8`, `etapa-4-e2e`).
- Convencion de commits: titulo en espanol describiendo la etapa o el requisito.
