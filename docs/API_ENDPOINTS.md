# API REST de Smartbody

Base local: `http://localhost:8080` (emulador de Android: `http://10.0.2.2:8080`; celular por USB con `adb reverse tcp:8080 tcp:8080`: `http://127.0.0.1:8080`).

Todas las respuestas son JSON con codificacion UTF-8.

## Autenticacion

La API es **stateless**: no hay sesiones en el servidor. Tras el login la aplicacion
guarda el JWT y lo envia en cada peticion protegida:

```http
Authorization: Bearer <token>
```

El token se firma con **RS256** (clave privada RSA que solo existe en el backend) y
las contrasenas se almacenan con **BCrypt**.

## Codigos de estado

| Codigo | Significado                       |
|--------|-----------------------------------|
| 200    | Operacion correcta                |
| 201    | Recurso creado                    |
| 400    | Datos invalidos                   |
| 401    | No autenticado / token invalido   |
| 403    | Sin permisos                      |
| 404    | Recurso no encontrado             |
| 409    | Conflicto (por ejemplo, correo ya registrado) |
| 500    | Error del servidor                |

Formato de error:

```json
{
  "estado": 400,
  "mensaje": "Revisa los datos ingresados.",
  "errores": { "email": "El formato del correo no es valido" },
  "momento": "2026-09-09T11:08:25.72"
}
```

---

## Autenticacion y cuenta

### GET /api/ping — publico

Comprueba que la aplicacion alcanza el backend.

```json
{ "servicio": "Smartbody API", "estado": "ACTIVO", "momento": "..." }
```

### POST /api/auth/register — publico — RF01

Peticion:

```json
{ "nombre": "Ana Prueba", "email": "ana@smartbody.com", "password": "Clave12345" }
```

Validaciones: nombre 2-100 caracteres, correo con formato valido y no duplicado,
contrasena de 8 a 72 caracteres.

Respuesta `201`:

```json
{
  "token": "eyJhbGciOiJSUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEnSegundos": 10800,
  "usuario": { "id": 2, "nombre": "Ana Prueba", "email": "ana@smartbody.com", "perfilCompleto": false }
}
```

Errores: `400` datos invalidos, `409` correo ya registrado.

### POST /api/auth/login — publico — RF02

Peticion:

```json
{ "email": "demo@smartbody.com", "password": "Smartbody123" }
```

Respuesta `200`: igual que el registro. Error `401` con credenciales incorrectas
o cuenta desactivada.

### POST /api/auth/logout — protegido — RF12

Confirma el cierre de sesion. Al ser una autenticacion stateless, la invalidacion
real ocurre en el cliente, que elimina el token guardado.

```json
{ "mensaje": "Sesion cerrada correctamente." }
```

### GET /api/users/me — protegido

Datos de la cuenta autenticada. Nunca devuelve la contrasena ni su hash.

```json
{ "id": 1, "nombre": "Usuario Demo", "email": "demo@smartbody.com", "perfilCompleto": true }
```

Errores: `401` sin token, con token invalido o expirado.

---

## Resumen de endpoints protegidos

Todos requieren `Authorization: Bearer <token>`. Cada consulta filtra por el usuario del
token: pedir una rutina o un plan de otro usuario responde `404` (RNF06).

| Metodo | Endpoint | Descripcion | Requisito |
|--------|----------|-------------|-----------|
| GET | `/api/users/summary` | Datos de la pantalla de inicio | RF11 |
| GET | `/api/users/profile` | Perfil fisico, objetivo y restricciones | RF03-RF05 |
| PUT | `/api/users/profile` | Crea o actualiza el perfil | RF03-RF05 |
| PUT | `/api/users/photo` | Sube o reemplaza la foto de perfil (multipart, campo `foto`) | RF03 |
| GET | `/api/users/photo` | Imagen de la foto de perfil (`404` si no tiene) | RF03 |
| DELETE | `/api/users/photo` | Quita la foto de perfil | RF03 |
| GET | `/api/objectives` | Catalogo de objetivos | RF04 |
| GET | `/api/restrictions` | Catalogo de restricciones | RF05 |
| POST | `/api/routines/generate` | Genera la rutina y la deja activa | RF06 |
| GET | `/api/routines/active` | Rutina vigente (`404` si no existe) | RF07 |
| GET | `/api/routines` | Historial de rutinas | RF07 |
| GET | `/api/routines/{id}` | Detalle de una rutina propia | RF07 |
| GET | `/api/exercises/{id}` | Descripcion e instrucciones de un ejercicio | RF08 |
| GET | `/api/routines/{id}/items/{itemId}/alternatives` | Ejercicios que pueden reemplazar a uno | RF09 |
| POST | `/api/routines/{id}/replace-exercise` | Sustituye un ejercicio | RF09 |
| POST | `/api/nutrition/generate` | Genera el plan nutricional | RF06 |
| GET | `/api/nutrition/active` | Plan vigente (`404` si no existe) | RF07 |
| GET | `/api/nutrition/{id}/items/{itemId}/alternatives` | Alimentos que pueden reemplazar a uno | RF09 |
| POST | `/api/nutrition/{id}/replace-food` | Sustituye un alimento | RF09 |
| POST | `/api/chat` | Pregunta al asistente (chatbot) | Descripcion del proyecto |
| GET | `/api/progress` | Historial de medidas y resumen | RF10 |
| POST | `/api/progress` | Registra nuevas medidas | RF10 |

---

## Perfil (RF03, RF04, RF05)

### PUT /api/users/profile

```json
{
  "edad": 30, "sexo": "FEMENINO", "pesoKg": 62, "alturaCm": 165,
  "nivelExperiencia": "INTERMEDIO", "minutosPorSesion": 30, "diasPorSemana": 5,
  "objetivoId": 2, "restriccionIds": [2, 12]
}
```

Validaciones: edad 12-100, peso 25-350 kg, altura 100-250 cm, sesion de 15 a 180 minutos,
de 1 a 7 dias, objetivo existente y restricciones existentes. Valores de `sexo`:
`MASCULINO`, `FEMENINO`, `OTRO`. Valores de `nivelExperiencia`: `PRINCIPIANTE`,
`INTERMEDIO`, `AVANZADO`.

Respuesta `200` (tambien de `GET /api/users/profile`):

```json
{
  "edad": 25, "sexo": "MASCULINO", "pesoKg": 75.5, "alturaCm": 175.0,
  "nivelExperiencia": "PRINCIPIANTE", "minutosPorSesion": 45, "diasPorSemana": 3,
  "objetivo": { "id": 1, "codigo": "PERDER_PESO", "nombre": "Perder peso", "descripcion": "..." },
  "restricciones": [ { "id": 1, "codigo": "LESION_RODILLA", "tipo": "SALUD", "nombre": "Lesion de rodilla", "descripcion": "..." } ],
  "imc": 24.65, "clasificacionImc": "Peso saludable", "completo": true
}
```

Si el peso o la altura cambian, se agrega automaticamente un registro al historial de progreso.

### PUT /api/users/photo

Peticion `multipart/form-data` con el archivo en el campo `foto`. Respuesta `200`:
`{ "mensaje": "Foto de perfil actualizada." }`.

- Solo se aceptan imagenes JPG, PNG o WEBP. El tipo se verifica por el contenido real del
  archivo, no por la extension ni por el tipo que declara el cliente.
- Tamano maximo de 700 KB (por debajo del `max_allowed_packet` de 1 MB de MySQL en XAMPP).
  La aplicacion recorta la foto en cuadrado, corrige la orientacion y la reduce a un JPEG
  de 512 px antes de subirla.
- Se guarda en la tabla `fotos_perfil`, separada de `usuarios` para no cargar la imagen en
  cada consulta. Cada usuario solo puede ver, cambiar o quitar su propia foto.

Errores `400`: "El archivo debe ser una imagen JPG, PNG o WEBP.", "La foto no puede superar 700 KB."

### GET /api/users/summary

```json
{
  "nombre": "Usuario Demo", "email": "demo@smartbody.com", "perfilCompleto": true,
  "objetivo": "Perder peso", "pesoKg": 75.5, "imc": 24.65, "clasificacionImc": "Peso saludable",
  "diasPorSemana": 3, "minutosPorSesion": 45, "rutinaActivaId": 1, "planActivoId": 1,
  "diasDesdeRutina": 0, "sugerirCambioRutina": false
}
```

`sugerirCambioRutina` es `true` cuando la rutina tiene 28 dias o mas (aviso de RF11).

---

## Rutinas (RF06 - RF09)

### POST /api/routines/generate

No requiere cuerpo. Exige un perfil completo (`400` "Completa tu perfil antes de generar
tu plan." en caso contrario). La rutina anterior queda inactiva. Respuesta `201`:

```json
{
  "id": 1, "nombre": "Rutina para perder peso",
  "objetivo": { "id": 1, "codigo": "PERDER_PESO", "nombre": "Perder peso" },
  "diasPorSemana": 3, "minutosPorSesion": 45, "activa": true, "generadaEn": "2026-09-16T13:32:02",
  "dias": [
    {
      "numero": 1, "enfoque": "Cuerpo completo", "minutosEstimados": 41,
      "ejercicios": [
        { "id": 1, "ejercicioId": 5, "nombre": "Puente de gluteos", "grupoMuscular": "PIERNAS", "tipo": "FUERZA", "series": 3, "repeticiones": 15, "descansoSeg": 45 },
        { "id": 6, "ejercicioId": 29, "nombre": "Eliptica", "grupoMuscular": "CARDIO", "tipo": "CARDIO", "series": 1, "duracionSeg": 960, "descansoSeg": 0 }
      ]
    }
  ]
}
```

En cada ejercicio, `id` identifica el item de la rutina y `ejercicioId` el ejercicio del
catalogo. Los ejercicios isometricos y de cardio usan `duracionSeg` en lugar de `repeticiones`.

### Motor de reglas

| Variable | Regla |
|----------|-------|
| Objetivo | Ganar masa: 4x10, descanso 90 s, sin cardio. Perder peso: 3x15, 45 s, 35 % cardio. Resistencia: 3x18, 30 s, 40 % cardio. Mantener: 3x12, 60 s, 20 % cardio. Condicion fisica: 3x12, 60 s, 25 % cardio. |
| Nivel | Principiante: maximo 3 series y solo ejercicios de nivel principiante. Avanzado: una serie extra. |
| Dias | 1-3 dias: cuerpo completo. 4 dias: tren superior / tren inferior. 5-7 dias: empuje / traccion / pierna. |
| Tiempo | Se calcula cuantos ejercicios caben en los minutos disponibles; 3 minutos de movilidad al final en sesiones de 30 minutos o mas. |
| Restricciones | Los ejercicios contraindicados (`ejercicio_restricciones`) nunca se incluyen. |

### POST /api/routines/{id}/replace-exercise

```json
{ "itemId": 2, "nuevoId": 11 }
```

`nuevoId` debe ser una de las alternativas de
`GET /api/routines/{id}/items/{itemId}/alternatives`, que devuelve ejercicios del mismo tipo
y grupo muscular, compatibles con el nivel y las restricciones y que no esten ya en ese dia.
Si se omite `nuevoId`, se usa la primera alternativa. Responde la rutina actualizada.
Error `400` si el ejercicio no es compatible.

### GET /api/exercises/{id}

```json
{
  "id": 5, "nombre": "Puente de gluteos", "descripcion": "Activacion de gluteos de bajo impacto.",
  "grupoMuscular": "PIERNAS", "equipamiento": "NINGUNO", "tipo": "FUERZA", "nivelMinimo": "PRINCIPIANTE",
  "instrucciones": "Boca arriba con rodillas flexionadas, eleva la cadera apretando los gluteos."
}
```

---

## Nutricion

### POST /api/nutrition/generate

Respuesta `201` (tambien de `GET /api/nutrition/active`):

```json
{
  "id": 1, "caloriasObjetivo": 2280, "caloriasTotales": 2068,
  "proteinaG": 150.8, "carbohidratosG": 221.5, "grasasG": 70.6,
  "comidas": [
    {
      "momento": "DESAYUNO", "nombre": "Desayuno", "calorias": 567,
      "alimentos": [
        { "id": 1, "alimentoId": 3, "nombre": "Huevo", "grupo": "PROTEINA", "cantidad": 175.0, "unidad": "g",
          "calorias": 271, "proteinaG": 22.8, "carbohidratosG": 1.9, "grasasG": 19.3,
          "descripcion": "Proteina completa y economica.", "alternativa": "Pechuga de pollo" }
      ]
    }
  ]
}
```

Cada comida incluye ademas una `receta` y el plan incluye la `hidratacion`:

```json
"receta": {
  "titulo": "Pechuga de pollo con arroz integral y brócoli",
  "ingredientes": ["215 g de pechuga de pollo", "150 g de arroz integral", "210 g de brócoli"],
  "pasos": ["Sazona la pechuga con sal, pimienta y ajo...", "...", "Sirve la proteína junto al acompañamiento..."],
  "bebida": "Un vaso de agua o limonada natural sin azúcar."
},
"hidratacion": {
  "litrosDiaEntrenamiento": 3.0, "litrosDiaDescanso": 2.6, "vasosDiaEntrenamiento": 12,
  "recomendaciones": ["Bebe un vaso de agua al despertar.", "..."],
  "evitar": ["Gaseosas y jugos con azúcar añadida.", "Bebidas energéticas.", "..."]
}
```

- Cada alimento (del plan y de las alternativas) incluye `icono`, un emoji ilustrativo guardado en
  `alimentos.icono` (por ejemplo "🍗" o "🥦"). La aplicacion lo muestra junto a cada alimento y
  en la pantalla para cambiar alimento. Jackson lo envia escapado (`🍗`), que es JSON valido.
- La receta se arma con los alimentos actuales de la comida (usa la columna `alimentos.preparacion`),
  por eso refleja las sustituciones. El aceite aparece en los ingredientes pero no en el titulo.
- Agua: 35 ml por kg de peso en dias de descanso, mas 500 ml por hora de entrenamiento.
  Las recomendaciones cambian segun el objetivo y las restricciones (por ejemplo, bebida de soya
  en lugar de leche para intolerancia a la lactosa o dieta vegana).

Reglas: calorias con la ecuacion de Mifflin-St Jeor, multiplicadas por un factor de
actividad segun los dias de entrenamiento (1,375 / 1,55 / 1,725) y ajustadas por objetivo
(-15 % perder peso, +10 % ganar masa), con un minimo de 1200 kcal. Cada alimento solo se propone en las
comidas indicadas en `alimentos.momentos` (por ejemplo, avena en desayuno o snack); si las restricciones
no dejan opciones, se usa cualquier alimento compatible del grupo. Se reparten en desayuno
25 %, almuerzo 35 %, cena 25 % y snack 15 %. Los alimentos incompatibles con las
restricciones alimentarias (`alimento_restricciones`) se descartan.

### POST /api/nutrition/{id}/replace-food

```json
{ "itemId": 1, "nuevoId": 1 }
```

El nuevo alimento debe ser del mismo grupo y compatible con las restricciones. La cantidad se
recalcula para mantener aproximadamente las mismas calorias.

---

## Progreso (RF10)

### POST /api/progress

```json
{ "pesoKg": 74.8, "cinturaCm": 87.5, "caderaCm": null, "pechoCm": null, "brazoCm": null }
```

Solo el peso es obligatorio. Si no se envia la altura, se usa la del perfil para calcular
el IMC. El perfil se actualiza con el nuevo peso. Respuesta `201`:

```json
{ "id": 4, "pesoKg": 74.8, "alturaCm": 175.0, "cinturaCm": 87.5, "imc": 24.42, "registradoEn": "2026-09-16T13:32:35" }
```

### GET /api/progress

```json
{
  "medidas": [ { "id": 1, "pesoKg": 78.0, "imc": 25.47, "registradoEn": "2026-08-17T05:37:51" } ],
  "pesoInicial": 78.0, "pesoActual": 74.8, "cambioPesoKg": -3.2,
  "imcActual": 24.42, "clasificacionImc": "Peso saludable"
}
```

Las medidas se devuelven de la mas antigua a la mas reciente.


---

## Chatbot

### POST /api/chat

```json
{ "mensaje": "¿Cuánta agua debo tomar?" }
```

Respuesta `200`:

```json
{
  "respuesta": "💧 Tu meta de hidratación:\n• Días de entrenamiento: 3 litros (unos 12 vasos)\n...",
  "sugerencias": ["¿Cuántas calorías debo comer?", "¿Qué ejercicios tengo en el día 1?"]
}
```

- Mensaje obligatorio, de hasta 500 caracteres.
- Tono: llama al usuario por su primer nombre, es muy amable y cierra cada respuesta con una frase
  que lo motiva a seguir entrenando. Evita palabras con genero. Ante dolor o lesiones el tono es igual
  de carinoso, pero prioriza la salud y no invita a entrenar.
- El asistente del MVP (`AsistentePorReglas`) detecta la intencion por palabras clave, sin
  importar tildes ni mayusculas, y responde con los datos reales del usuario: rutina (y ejercicios
  de un dia concreto), comidas, recetas, macronutrientes, hidratacion, IMC y progreso, como cambiar
  ejercicios o alimentos, descanso, motivacion y uso de la aplicacion.
- Ante palabras como "dolor", "lesión" o "mareo" siempre recomienda detenerse y consultar a un
  profesional de la salud; nunca da diagnosticos.
- No guarda la conversacion en el servidor. La aplicacion la conserva en memoria durante la sesion y
  la borra al cerrar sesion o al iniciar con otro usuario.
- Esta detras de la interfaz `AgenteConversacional`: un agente de IA puede reemplazarlo desde el
  backend (sin exponer claves en la aplicacion) sin cambiar el endpoint.

---

## Administracion (solo `ROLE_ADMIN`)

Un usuario autenticado sin rol de administrador recibe `403`. Sin token, `401`.

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/admin/summary` | Estadisticas de uso e historial de actividad reciente |
| GET | `/api/admin/users` | Usuarios con estado, perfil, objetivo, rutinas y medidas |
| PUT | `/api/admin/users/{id}/status` | Activa o desactiva una cuenta: `{ "activo": false }` |
| GET | `/api/admin/users/{id}` | Detalle: cuenta, perfil, progreso, rutina activa y plan nutricional |
| POST | `/api/admin/users/{id}/plan` | Genera una rutina y un plan nutricional nuevos con el perfil actual del usuario |
| GET | `/api/admin/reports/effectiveness` | Reporte de efectividad: usuarios que avanzan hacia su objetivo |

### GET /api/admin/summary

```json
{
  "totalUsuarios": 3, "usuariosActivos": 3, "usuariosNuevosUltimos30Dias": 3, "perfilesCompletos": 2,
  "rutinasGeneradas": 3, "planesGenerados": 3, "medidasRegistradas": 4,
  "usuariosPorObjetivo": [ { "nombre": "Perder peso", "cantidad": 2 } ],
  "restriccionesMasComunes": [ { "nombre": "Lesión de rodilla", "cantidad": 2 } ],
  "actividadReciente": [
    { "tipo": "PLAN", "descripcion": "Generó un plan nutricional de 2280 kcal", "usuario": "Usuario Demo",
      "email": "demo@smartbody.com", "fecha": "2026-09-16T15:38:09" }
  ]
}
```

`actividadReciente` es el historial de uso: los 20 eventos mas recientes (registros, rutinas, planes
nutricionales y medidas).

### PUT /api/admin/users/{id}/status

- Una cuenta desactivada no puede iniciar sesion (`401` "La cuenta se encuentra desactivada.") y su
  token deja de ser aceptado de inmediato.
- Las cuentas de administrador no se pueden desactivar (`400`).

### GET /api/admin/reports/effectiveness

Compara la primera y la ultima medida de cada usuario con al menos dos registros:

| Objetivo | Se considera que avanza si... |
|----------|-------------------------------|
| Perder peso | bajo de peso |
| Ganar masa muscular | subio de peso |
| Mantener peso | vario maximo 1 kg |
| Mejorar condicion fisica o resistencia | su IMC se acerco al rango saludable (18,5-24,9) o sigue dentro de el |

```json
{
  "usuariosConPerfil": 7, "usuariosConSeguimiento": 6, "usuariosQueMejoraron": 5,
  "porcentajeMejora": 83.3, "promedioDiasSeguimiento": 52.3,
  "porObjetivo": [ { "objetivo": "Perder peso", "usuarios": 3, "mejoraron": 2, "porcentajeMejora": 66.7, "promedioCambioPesoKg": -1.6 } ],
  "usuarios": [ { "id": 4, "nombre": "Laura Gómez", "objetivo": "Perder peso", "pesoInicial": 70.0, "pesoActual": 66.5,
                  "cambioPesoKg": -3.5, "imcInicial": 26.67, "imcActual": 25.34, "mejoro": true, "diasSeguimiento": 59 } ]
}
```

### POST /api/admin/users/{id}/plan

Usa las mismas reglas que la aplicacion (restricciones, nivel, dias y minutos del usuario). Responde `400` si el
usuario no ha completado su perfil o si la cuenta es de administrador.

### Cuenta de administrador

No hay administrador en los scripts SQL. El backend lo crea al arrancar si se definen estas
variables de entorno; la contrasena nunca queda en el codigo:

| Variable | Descripcion |
|----------|-------------|
| `SMARTBODY_ADMIN_EMAIL` | Correo del administrador |
| `SMARTBODY_ADMIN_PASSWORD` | Contrasena (minimo 10 caracteres). Solo se usa si la cuenta no existe |
| `SMARTBODY_ADMIN_NOMBRE` | Nombre visible (por defecto "Administrador") |

Si el correo ya existe, solo se le agrega el rol de administrador sin cambiar su contrasena.

> Las **ventas** que menciona la historia de usuario del administrador no se reportan: la aplicacion
> no tiene pagos ni suscripciones, que estan fuera del MVP (*Won't have*).
