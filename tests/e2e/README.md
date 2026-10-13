# Pruebas end-to-end de la API (Etapa 4)

Smartbody no tiene interfaz web, asi que el equivalente de Cypress es una coleccion de
**Postman** ejecutada con **Newman** (su CLI open source) contra el backend desplegado.
`smartbody.postman_collection.json` recorre el flujo completo de un usuario (21 peticiones):

ping → ruta protegida sin token (401) → registro → registro duplicado (409) → login incorrecto
(401) → login → perfil → rutina sin perfil (400) → catalogo → completar perfil → generar
rutina → rutina activa → alternativas → plan nutricional → plan activo → registrar medidas →
progreso → chatbot → resumen → panel de admin sin rol (403) → cerrar sesion.

Cada ejecucion registra un usuario nuevo (`e2e-<timestamp>@smartbody.com`), por lo que se puede
repetir sin limpiar la base de datos.

## En el pipeline

El job `k8s:kind` de `.gitlab-ci.yml` despliega la aplicacion en un cluster Kubernetes efimero
y luego ejecuta la coleccion a traves de Nginx. El resultado aparece en la pestana **Tests** del
pipeline (reporte JUnit `e2e-api.xml`).

## En local

```bash
npm install -g newman
newman run tests/e2e/smartbody.postman_collection.json --env-var baseUrl=http://localhost:8080
```

O importa el archivo en Postman y define la variable `baseUrl`.
