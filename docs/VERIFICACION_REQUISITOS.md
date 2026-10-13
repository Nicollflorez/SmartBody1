# Verificacion de requisitos — Smartbody

Fecha: 16 de septiembre de 2026.

Fuentes: documento *Especificacion de requisitos IEEE 830 (enfoque agil)* y criterios de aceptacion
del MVP.

## Como se verifico

1. **Prueba automatica de extremo a extremo** (56 verificaciones) contra el backend en ejecucion y
   MySQL de XAMPP. Crea dos usuarios temporales, recorre todos los requisitos, comprueba en la base de
   datos lo que la API no muestra (hash BCrypt, contraindicaciones, incompatibilidades) y al final
   borra los usuarios. **Resultado: 56 de 56 correctas.**
2. **Pruebas unitarias:** 85 en el backend y 18 en Android. **Todas correctas.**
3. **Aplicacion en un celular real** (Xiaomi, Android 15) conectado por USB.
4. **Revision del codigo** para seguridad, arquitectura y documentacion.

---

## Requisitos funcionales

| Requisito | Prioridad | Estado | Evidencia |
|-----------|-----------|--------|-----------|
| RF01 Registro de usuarios | Must | ✅ Cumple | 201 con JWT; correo duplicado da 409; nombre, correo y contraseña se validan (400) |
| RF02 Inicio de sesion | Must | ✅ Cumple | JWT RS256; contraseña incorrecta da 401; token manipulado da 401 |
| RF03 Datos personales | Must | ✅ Cumple | Edad, sexo, peso, altura, nivel, dias y minutos; IMC calculado; validaciones; foto de perfil |
| RF04 Seleccion de objetivos | Must | ✅ Cumple | 5 objetivos; el objetivo cambia series, repeticiones, descanso, cardio y calorias |
| RF05 Restricciones | Must | ✅ Cumple | 6 de salud y 6 de alimentacion; aviso de que no sustituye a un profesional |
| RF06 Generacion de rutina | Must | ✅ Cumple | Respeta dias (4 de 4), minutos (29-30 de 30), nivel y restricciones (0 contraindicados); tambien genera el plan nutricional |
| RF07 Visualizacion | Must | ✅ Cumple | Rutina por dias con series, repeticiones o tiempo y descanso; plan por comidas con calorias, macronutrientes, receta e hidratacion |
| RF08 Detalle de ejercicios | Must | ✅ Cumple | Nombre, descripcion, grupo muscular, equipo, prescripcion e instrucciones paso a paso |
| RF09 Sustitucion | Should | ✅ Cumple | Ejercicios y alimentos; solo alternativas compatibles; un ejercicio contraindicado se rechaza (400) |
| RF10 Actualizacion de medidas | Should | ✅ Cumple | Registro con IMC, validacion, historial y grafico de evolucion |
| RF11 Notificaciones | Could | ⚠️ Parcial | Aviso dentro de la aplicacion a los 28 dias (probado con una rutina de 30 dias). **Sin notificaciones push** |
| RF12 Cerrar sesion | Should | ✅ Cumple | Confirmacion; se borra el token local y la pila de navegacion |

## Requisitos no funcionales

| Requisito | Estado | Evidencia |
|-----------|--------|-----------|
| RNF01 Disponibilidad 99 % | ➖ No medible en local | El sistema funciona mientras XAMPP y el backend esten encendidos. El 99 % solo puede medirse en un despliegue real |
| RNF02 Seguridad | ✅ Cumple | BCrypt (`$2a$10$`), JWT RS256 con clave privada solo en el backend, validacion en app y backend, sin secretos en el codigo, foto validada por su contenido. HTTPS queda pendiente para el despliegue |
| RNF03 Rutina en menos de 10 s | ✅ Cumple | 304 ms |
| RNF04 Interfaz en menos de 3 s | ✅ Cumple | Operacion mas lenta: 1,7 s (registro, por el costo intencional de BCrypt); el resto, en milisegundos |
| RNF05 Usabilidad | ✅ Cumple | Mensajes claros sin errores tecnicos, validaciones por campo, textos con tildes, navegacion por pestañas |
| RNF06 Privacidad | ✅ Cumple | Sin token, 401 en todos los endpoints; otro usuario recibe 404 al pedir rutina, plan o foto ajenos, y no ve medidas ajenas; el chat se borra al cambiar de usuario |

## Historias de usuario

| Historia | Estado |
|----------|--------|
| **Usuario:** registrarse, medidas, objetivo, rutinas, nutricion, restricciones, cambiar rutina y alimentacion, aviso de cambio de rutina | ✅ Cumple (aviso dentro de la app, sin push) |
| **Agente IA:** generar rutina y dieta con los datos y restricciones | ✅ Cumple con motor de reglas; preparado para IA real (`GeneradorRutina`, `GeneradorPlanNutricional`, `AgenteConversacional`) |
| **Chatbot** (descripcion del proyecto) | ✅ Cumple con asistente por reglas que responde con los datos del usuario |
| **Administrador:** acceso a todos los modulos, historiales de uso y resumenes de ventas | ✅ Tiene todos los modulos de usuario (Inicio, Rutina, Nutricion, Progreso, Perfil, chat) y ademas su panel desde Inicio y Perfil: estadisticas, historial de uso, reporte de efectividad, detalle de cada usuario (perfil, progreso, rutina y plan), generar un nuevo plan para un usuario y activar o desactivar cuentas. ⚠️ Sin resumen de ventas: el MVP no tiene pagos (*Won't have*) |

## Diagrama de casos de uso

| Caso de uso | Estado |
|-------------|--------|
| Registrarme, crear credenciales, ingresar al sistema | ✅ |
| Ingreso de datos, registrar medidas (peso, altura), restricciones fisicas y nutricionales | ✅ |
| Procesar datos, generar y ver rutina, cambiar rutina | ✅ |
| Generar, ver y cambiar plan nutricional | ✅ |
| Generar notificaciones | ⚠️ Solo aviso dentro de la app |
| Chatbot | ✅ |
| Generar y ver reporte (administrador) | ✅ Resumen de uso, historial de actividad y reporte de efectividad por objetivo |

## Descripcion del proyecto

| Elemento | Estado |
|----------|--------|
| Rutinas adaptadas a la disponibilidad diaria | ✅ |
| Guia nutricional con recetas | ✅ Receta por comida e hidratacion |
| Seguimiento semanal o mensual del avance | ✅ Historial y grafico de medidas |
| Plan **por fases** para llegar al objetivo y mantenerlo | ❌ No implementado: hay un plan vigente que se puede regenerar, pero no fases |

## Criterios de aceptacion del MVP

| Criterio | Estado |
|----------|--------|
| La aplicacion Android compila | ✅ |
| El backend compila y se ejecuta | ✅ |
| MySQL funciona con XAMPP | ✅ |
| Android se comunica con el backend local | ✅ (celular por USB con `adb reverse`) |
| Registro, login y JWT | ✅ |
| Endpoints protegidos rechazan peticiones sin JWT valido | ✅ |
| Contraseñas con BCrypt | ✅ |
| Datos, objetivo y restricciones | ✅ |
| Generar y ver rutina; consultar ejercicios | ✅ |
| Consultar plan nutricional | ✅ |
| Actualizar medidas | ✅ |
| Cerrar sesion | ✅ |
| Datos de cada usuario aislados | ✅ |
| Sin errores criticos de compilacion | ✅ |
| Documentacion para instalar y ejecutar | ✅ |

## Correcciones posteriores a la verificacion

- **Modulo de administracion:** cuenta de administrador creada por variables de entorno, panel con
  estadisticas, historial de uso y activacion o desactivacion de cuentas. Verificado: 401 sin token, 403 para
  usuarios normales, 200 para el administrador; una cuenta desactivada pierde la sesion y no puede entrar.
- **Momento del dia de cada alimento:** antes el plan podia proponer avena en el almuerzo. Ahora cada
  alimento indica en que comidas se recomienda (`alimentos.momentos`). Se verifico generando 25 planes
  contra la base de datos real: 0 alimentos fuera de su momento del dia. Pruebas unitarias: 72 en el backend.

## Pendientes

1. **Pruebas manuales en el celular** de las pantallas nuevas (CP39 a CP47 en
   [CASOS_DE_PRUEBA.md](CASOS_DE_PRUEBA.md)). El backend que consumen esta verificado, pero el recorrido
   de la interfaz debe hacerse a mano y documentarse con capturas.
2. **Token expirado dentro de la aplicacion:** el backend lo rechaza (prueba automatica) y la app
   vuelve al login ante un 401, pero no se probo esperando a que el token expire en el dispositivo.
3. **Resumen de ventas del administrador:** requiere pagos, fuera del MVP; conviene declararlo *Won't have*.
4. **Notificaciones push (RF11, Could have)** y **plan por fases:** posibles mejoras futuras.
5. **RNF01 y HTTPS:** aplican cuando el sistema se despliegue en un servidor.
