# Casos de prueba

Entorno de ejecucion de las pruebas:

- Backend Spring Boot en `http://localhost:8080`
- MySQL/MariaDB 10.4 de XAMPP con `smartbody_db` creada desde los scripts de `database/`
- Fases 1 y 2: emulador Android Pixel 3a, API 30, apuntando a `http://10.0.2.2:8080/`
- Fases 3 a 6: celular Xiaomi con Android 15 conectado por USB, apuntando a
  `http://127.0.0.1:8080/` con `adb reverse tcp:8080 tcp:8080`

---

## 1. Pruebas automatizadas

### Backend: `./mvnw test`

`Tests run: 85, Failures: 0, Errors: 0`

| Clase | Caso | Resultado |
|-------|------|-----------|
| `JwtServiceTest` | Genera un token valido para el usuario | OK |
| `JwtServiceTest` | Rechaza un token manipulado | OK |
| `JwtServiceTest` | Rechaza un texto que no es un token | OK |
| `JwtServiceTest` | Rechaza un token expirado | OK |
| `JwtServiceTest` | Rechaza un token firmado con otra clave | OK |
| `AuthServiceTest` | Registra guardando la contrasena con BCrypt (nunca en texto plano) | OK |
| `AuthServiceTest` | Rechaza el registro con correo duplicado | OK |
| `AuthServiceTest` | Inicia sesion con credenciales correctas | OK |
| `AuthServiceTest` | Rechaza el login con contrasena incorrecta (401) | OK |
| `AuthServiceTest` | Rechaza el login de un usuario inexistente | OK |
| `AuthServiceTest` | Rechaza el login de una cuenta desactivada | OK |
| `PerfilServiceTest` | Guarda el perfil, calcula el IMC y registra la medida inicial | OK |
| `PerfilServiceTest` | Rechaza un objetivo inexistente | OK |
| `AdminServiceTest` | Desactiva la cuenta de un usuario | OK |
| `EvaluadorProgresoTest` | Evalua si el usuario avanza segun su objetivo (9 casos) | OK |
| `ReporteServiceTest` | Calcula el porcentaje de usuarios que mejoraron segun su objetivo | OK |
| `AdminServiceTest` | No permite desactivar una cuenta de administrador | OK |
| `AdminServiceTest` | El resumen reune las estadisticas y ordena la actividad de la mas reciente | OK |
| `FotoPerfilServiceTest` | Guarda una foto JPG del usuario | OK |
| `FotoPerfilServiceTest` | Detecta el tipo por el contenido y no por la extension | OK |
| `FotoPerfilServiceTest` | Rechaza archivos que no son imagenes aunque digan ser .jpg | OK |
| `FotoPerfilServiceTest` | Rechaza fotos de mas de 700 KB | OK |
| `FotoPerfilServiceTest` | Sin foto responde 404 | OK |
| `AsistentePorReglasTest` | Detecta la intencion aunque la pregunta tenga tildes (11 preguntas) | OK |
| `AsistentePorReglasTest` | Responde la hidratacion con los datos del usuario | OK |
| `AsistentePorReglasTest` | Muestra los ejercicios del dia solicitado | OK |
| `AsistentePorReglasTest` | Da la receta de la comida pedida | OK |
| `AsistentePorReglasTest` | Ante dolor recomienda detenerse y consultar un profesional | OK |
| `AsistentePorReglasTest` | Sin perfil completo pide completarlo | OK |
| `AsistentePorReglasTest` | Cuando no entiende ofrece sugerencias | OK |
| `AsistentePorReglasTest` | Llama al usuario por su nombre y lo motiva (7 preguntas) | OK |
| `AsistentePorReglasTest` | Ante dolor es amable sin invitar a entrenar | OK |
| `AsistentePorReglasTest` | Usa el primer nombre con mayuscula inicial | OK |
| `RutinaServiceTest` | Una rutina de otro usuario se responde como inexistente (404) | OK |
| `RutinaServiceTest` | No se puede sustituir un ejercicio en una rutina ajena | OK |
| `RutinaServiceTest` | No genera rutina sin perfil completo | OK |
| `GeneradorRutinaPorReglasTest` | Genera una sesion por cada dia disponible | OK |
| `GeneradorRutinaPorReglasTest` | Cada sesion se ajusta al tiempo disponible (6 combinaciones de objetivo, nivel y minutos) | OK |
| `GeneradorRutinaPorReglasTest` | Nunca incluye ejercicios contraindicados para las restricciones | OK |
| `GeneradorRutinaPorReglasTest` | No incluye ejercicios por encima del nivel del usuario | OK |
| `GeneradorRutinaPorReglasTest` | Perder peso incluye cardio y ganar masa no | OK |
| `GeneradorRutinaPorReglasTest` | Ganar masa usa menos repeticiones y mas descanso que perder peso | OK |
| `GeneradorRutinaPorReglasTest` | Las alternativas son del mismo grupo y respetan las restricciones | OK |
| `GeneradorRutinaPorReglasTest` | La sustitucion conserva el dia y el orden | OK |
| `GeneradorPlanNutricionalPorReglasTest` | Las calorias dependen del objetivo (Mifflin-St Jeor) | OK |
| `GeneradorPlanNutricionalPorReglasTest` | Nunca baja del minimo de calorias seguro | OK |
| `GeneradorPlanNutricionalPorReglasTest` | Incluye las cuatro comidas del dia | OK |
| `GeneradorPlanNutricionalPorReglasTest` | Excluye los alimentos incompatibles (vegano + intolerancia a la lactosa) | OK |
| `GeneradorPlanNutricionalPorReglasTest` | La sustitucion mantiene las calorias aproximadas | OK |
| `GeneradorPlanNutricionalPorReglasTest` | Las alternativas son del mismo grupo y compatibles | OK |
| `GeneradorPlanNutricionalPorReglasTest` | Cada receta usa los alimentos y cantidades de la comida | OK |
| `GeneradorPlanNutricionalPorReglasTest` | La hidratacion depende del peso y del tiempo de entrenamiento | OK |
| `GeneradorPlanNutricionalPorReglasTest` | Sugiere bebida vegetal cuando el usuario no consume lacteos | OK |
| `GeneradorPlanNutricionalPorReglasTest` | La avena solo se recomienda en el desayuno o el snack (40 planes) | OK |
| `GeneradorPlanNutricionalPorReglasTest` | Las alternativas respetan el momento del dia | OK |
| `GeneradorPlanNutricionalPorReglasTest` | Si ningun alimento es adecuado para la comida usa los disponibles | OK |

### Aplicacion Android: `./gradlew testDebugUnitTest`

`18 pruebas, 0 fallos`

| Clase | Caso | Resultado |
|-------|------|-----------|
| `ValidacionesTest` | Acepta un correo con formato valido | OK |
| `ValidacionesTest` | Rechaza correos con formato invalido | OK |
| `ValidacionesTest` | Rechaza el correo vacio | OK |
| `ValidacionesTest` | Exige contrasena de al menos 8 caracteres | OK |
| `ValidacionesTest` | Valida el nombre | OK |
| `ValidacionesTest` | Detecta contrasenas que no coinciden | OK |
| `ValidacionesTest` | Acepta coma o punto como separador decimal | OK |
| `ValidacionesTest` | Valida la edad dentro de un rango razonable | OK |
| `ValidacionesTest` | El peso y la altura deben ser mayores que cero y razonables | OK |
| `ValidacionesTest` | Las medidas opcionales pueden quedar vacias | OK |
| `FormatosTest` | Muestra series y repeticiones | OK |
| `FormatosTest` | Muestra el tiempo de ejercicios continuos e isometricos | OK |
| `FormatosTest` | Formatea duraciones | OK |
| `FormatosTest` | Convierte codigos en texto legible | OK |
| `FormatosTest` | Muestra numeros enteros sin decimales | OK |
| `FormatosTest` | Formatea la fecha de registro | OK |
| `ChatPresenterTest` | Saluda con el primer nombre del usuario | OK |
| `ChatPresenterTest` | Saluda aunque no conozca el nombre | OK |

---

## 2. Pruebas de la API contra MySQL (ejecutadas con `curl`)

| ID | Caso | Peticion | Esperado | Obtenido |
|----|------|----------|----------|----------|
| CP01 | Comunicacion con el backend | `GET /api/ping` | 200 y estado ACTIVO | 200 `{"servicio":"Smartbody API","estado":"ACTIVO",...}` |
| CP02 | Registro correcto | `POST /api/auth/register` con datos validos | 201 y token | 201 con JWT y datos del usuario |
| CP03 | Registro duplicado | Mismo correo dos veces | 409 con mensaje claro | 409 "Ya existe una cuenta registrada con ese correo." |
| CP04 | Validacion de datos | Nombre corto, correo invalido, clave de 3 caracteres | 400 con errores por campo | 400 con los tres errores identificados por campo |
| CP05 | Login correcto | `POST /api/auth/login` con el usuario demo | 200 y token | 200 con JWT (valida el hash BCrypt guardado en MySQL) |
| CP06 | Login con contrasena incorrecta | Contrasena erronea | 401 sin revelar si el correo existe | 401 "Correo o contrasena incorrectos." |
| CP07 | Endpoint protegido sin token | `GET /api/users/me` sin cabecera | 401 | 401 "Debes iniciar sesion para continuar." |
| CP08 | Endpoint protegido con token valido | `GET /api/users/me` con `Bearer <token>` | 200 con los datos del usuario | 200 `{"id":1,"nombre":"Usuario Demo",...}` |
| CP09 | Token manipulado | Token con caracteres anadidos | 401 | 401 "Debes iniciar sesion para continuar." |
| CP10 | Cierre de sesion | `POST /api/auth/logout` | 200 | 200 "Sesion cerrada correctamente." |
| CP17 | Consultar perfil | `GET /api/users/profile` (demo) | Perfil con IMC y restricciones | 200, IMC 24,65 "Peso saludable", restricciones rodilla e intolerancia a la lactosa |
| CP18 | Catalogos | `GET /api/objectives`, `GET /api/restrictions` | Listas de los datos iniciales | 200 con 5 objetivos y 12 restricciones |
| CP19 | Perfil invalido | `PUT /api/users/profile` con edad 5, peso 0 y 9 dias | 400 con errores por campo | 400 con errores en `edad`, `pesoKg` y `diasPorSemana` |
| CP20 | Guardar perfil | Usuario nuevo, vegano, lesion de hombro, 5 dias, 30 min | 200 y perfil completo | 200 `completo: true` |
| CP21 | Generar sin perfil | `POST /api/routines/generate` con usuario nuevo sin perfil | 400 con mensaje claro | 400 "Completa tu perfil antes de generar tu plan." |
| CP22 | Rutina inexistente | `GET /api/routines/active` antes de generar | 404 | 404 "Aun no tienes una rutina generada." |
| CP23 | Generar rutina (RNF03) | `POST /api/routines/generate` (demo: perder peso, 3 dias, 45 min) | 201 en menos de 10 s | 201 en 0,34 s; 3 dias "Cuerpo completo" de 41-42 min estimados |
| CP24 | Rutina segun el tiempo | Usuario de CP20 con 30 min y 5 dias | Sesiones de 30 min aprox. | 5 dias de 30 min con division empuje / traccion / pierna |
| CP25 | Restricciones respetadas | Demo con lesion de rodilla | Sin sentadillas, zancadas ni saltos | Ningun ejercicio contraindicado en la rutina |
| CP26 | Detalle del ejercicio | `GET /api/exercises/5` | Nombre, descripcion, grupo e instrucciones | 200 con todos los campos |
| CP27 | Alternativas de ejercicio | `GET /api/routines/1/items/2/alternatives` | Ejercicios del mismo grupo compatibles | 200: Remo con mancuerna, Jalon al pecho |
| CP28 | Sustituir ejercicio | `POST /api/routines/1/replace-exercise` `{"itemId":2}` | Rutina con el ejercicio cambiado | 200: Remo en polea baja -> Remo con mancuerna |
| CP29 | Sustituir por un ejercicio contraindicado | `{"itemId":8,"nuevoId":1}` (sentadilla con lesion de rodilla) | 400 | 400 "El ejercicio elegido no es compatible con tu rutina o tus restricciones." |
| CP30 | Generar plan nutricional | `POST /api/nutrition/generate` (demo) | 201 con 4 comidas | 201 en 0,12 s; meta 2280 kcal, 4 comidas con alternativa por alimento |
| CP31 | Restricciones alimentarias | Plan del demo (intolerancia a la lactosa) y del usuario vegano | Sin lacteos / sin productos animales | Ningun lacteo en el demo; el plan vegano solo usa legumbres, cereales, frutas, verduras y frutos secos |
| CP32 | Sustituir alimento | `POST /api/nutrition/1/replace-food` `{"itemId":1}` | Alimento cambiado con cantidad recalculada | 200: Huevo 175 g -> Pechuga de pollo 165 g |
| CP33 | Consultar progreso | `GET /api/progress` (demo) | Historial y resumen | 200: 3 medidas, de 78 a 75,5 kg (-2,5 kg) |
| CP34 | Registrar medidas | `POST /api/progress` `{"pesoKg":74.8,"cinturaCm":87.5}` | 201 con IMC calculado | 201, IMC 24,42 con la altura del perfil |
| CP35 | Medida invalida | `POST /api/progress` `{"pesoKg":-5}` | 400 | 400 "El peso debe ser mayor a 25 kg" |
| CP36 | Aislamiento entre usuarios (RNF06) | Otro usuario pide `GET /api/routines/1` y las alternativas del plan 1 del demo | 404 sin revelar datos | 404 en ambos casos |
| CP48 | Foto de perfil valida | `PUT /api/users/photo` con PNG de 287 KB y luego `GET` | 200 y la misma imagen | 200 `image/png`, bytes identicos, `Cache-Control: no-cache, private` |
| CP49 | Archivo que no es imagen | Texto con extension .jpg | 400 | 400 "El archivo debe ser una imagen JPG, PNG o WEBP." |
| CP50 | Foto demasiado grande | Archivo de 900 KB | 400 | 400 "La foto no puede superar 700 KB." |
| CP51 | Foto sin token y eliminacion | `GET` sin token; `DELETE` y luego `GET` | 401; 200 y despues 404 | 401; 200 y 404 |
| CP52 | Chatbot con datos del usuario | `POST /api/chat` con "hola", "¿Cuánta agua debo tomar?", "ejercicios del día 2", "receta de la cena", "¿cómo va mi progreso?" | 200 con respuestas personalizadas | Saludo por nombre; 3 L / 12 vasos; 7 ejercicios del día 2; receta completa de la cena; IMC 24,7 y -2,5 kg |
| CP53 | Chatbot ante dolor | "me duele la rodilla" | Recomienda detenerse y consultar a un profesional | Correcto, y menciona la lesión de rodilla registrada |
| CP54 | Chatbot: pregunta no reconocida, vacía o sin token | "abc xyz"; "  "; sin cabecera | Sugerencias; 400; 401 | Correcto en los tres casos |
| CP55 | Cuenta de administrador | Arrancar con `SMARTBODY_ADMIN_EMAIL` y `SMARTBODY_ADMIN_PASSWORD`; login | Cuenta creada con BCrypt y roles de usuario y administrador | Correcto; `esAdministrador: true` en el login |
| CP56 | Panel protegido por rol | `GET /api/admin/summary` sin token, como usuario normal y como administrador | 401, 403 y 200 | 401, 403 y 200 con estadisticas e historial |
| CP58 | Administrador usa la app completa y su panel | Login con la cuenta de administrador | Llega a Inicio con todos los modulos y la tarjeta "Panel de administracion" (tambien en Perfil); desde el panel vuelve con atras | `GET /api/users/summary` devuelve `esAdministrador: true`; usuario normal recibe `false` y 403 en `/api/admin/**` |
| CP59 | Detalle de usuario y nuevo plan | `GET /api/admin/users/{id}` y `POST /api/admin/users/{id}/plan` para Laura (vegana no, intolerante a la lactosa) | Perfil, progreso, rutina y plan; plan sin lacteos | 200; rutina de 4 dias y plan de 1697 kcal sin lacteos |
| CP60 | Reporte de efectividad | `GET /api/admin/reports/effectiveness` | Porcentaje global, por objetivo y por usuario | 83,3 % (5 de 6); Julian Perez sin avance (+1,1 kg con objetivo perder peso) |
| CP61 | Endpoints de administracion con usuario normal | Detalle, generar plan y reporte con el usuario demo | 403 | 403 en los tres; la cuenta de administrador no tiene detalle de plan (400) |
| CP57 | Desactivar cuentas | Desactivar a un usuario; intentar desactivar al administrador; usuario normal intenta reactivar | 200 y sesion invalida; 400; 403 | 200, token rechazado (401) y login rechazado; 400; 403 |

En ninguna respuesta de la API aparece la contrasena ni su hash.

---

## 3. Pruebas de la aplicacion

| ID | Caso | Pasos | Esperado | Obtenido | Evidencia |
|----|------|-------|----------|----------|-----------|
| CP11 | Pantalla de inicio de sesion | Abrir la aplicacion sin sesion previa | Se muestra el login | Correcto | `evidencias/01_pantalla_login.png` |
| CP12 | Login desde la aplicacion | Ingresar `demo@smartbody.com` / `Smartbody123` | Navega al inicio con los datos reales | Correcto | `evidencias/02_dashboard_tras_login.png` |
| CP13 | Token enviado automaticamente | Observar la peticion posterior al login | La peticion protegida incluye el JWT | Correcto gracias al interceptor de OkHttp | — |
| CP14 | Cierre de sesion (RF12) | Cerrar sesion | Vuelve al login y elimina el token | Correcto | — |
| CP15 | Pantalla protegida inaccesible | Tras cerrar sesion, volver a abrir la aplicacion | Se muestra el login | Correcto: el token fue eliminado de DataStore | `evidencias/03_tras_cerrar_sesion.png` |
| CP16 | Backend apagado | Detener el backend e intentar iniciar sesion | Mensaje comprensible | "El servidor no responde. Verifica que el backend este encendido e intenta nuevamente." | `evidencias/04_error_sin_backend.png` |
| CP37 | Login en el celular por USB | Iniciar sesion con el usuario demo | Llega al backend por `adb reverse` | Correcto | — |
| CP38 | Inicio con resumen del perfil | Abrir la aplicacion con sesion activa | Objetivo, peso, IMC, disponibilidad y accesos rapidos | Correcto: muestra "Perder peso", 74,8 kg, IMC 24,4 y la barra inferior | — |

### Pendiente de verificar manualmente en el celular

Estas pantallas estan implementadas y compilan, y el backend que consumen ya fue probado
(seccion 2), pero aun no se han recorrido a mano en el dispositivo:

| ID | Caso |
|----|------|
| CP39 | Asistente de perfil en 4 pasos con validaciones (datos, objetivo, disponibilidad, restricciones) |
| CP40 | Registro de un usuario nuevo que lleva directamente al asistente de perfil |
| CP41 | Generar plan desde la aplicacion y llegar a la pestana Rutina |
| CP42 | Rutina por dias y detalle de un ejercicio |
| CP43 | Sustituir un ejercicio y ver la rutina actualizada |
| CP44 | Plan nutricional por comidas y sustitucion de un alimento |
| CP45 | Registrar medidas y ver el grafico y el historial de progreso |
| CP46 | Pestana Perfil y cierre de sesion con confirmacion |
| CP47 | Token expirado en la aplicacion: cualquier pantalla vuelve al login |
