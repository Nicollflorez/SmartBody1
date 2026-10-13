# Arquitectura de Smartbody

## Vision general

```text
┌──────────────────────────────────────────────┐
│              APLICACION ANDROID              │
│                                              │
│   VIEW (Jetpack Compose + Material 3)        │
│     Splash · Login · Registro · Inicio       │
│     Perfil · Rutina · Nutricion · Progreso   │
│                    ▲ estado    │ eventos     │
│                    │           ▼             │
│   PRESENTER                                  │
│     Valida, llama al Model y decide          │
│     que debe mostrar la View                 │
│                    ▲           │             │
│                    │           ▼             │
│   MODEL                                      │
│     DTO · Repository · Resultado/errores     │
│                    ▲           │             │
│                    │           ▼             │
│   NETWORK          SECURITY                  │
│     Retrofit         TokenManager            │
│     OkHttp           (DataStore)             │
│     AuthInterceptor                          │
└───────────────────────┬──────────────────────┘
                        │  HTTP + JSON
                        │  Authorization: Bearer <JWT>
                        ▼
┌──────────────────────────────────────────────┐
│                   BACKEND                    │
│                                              │
│   SECURITY                                   │
│     JwtAuthenticationFilter → JwtService     │
│     RsaKeyProvider (claves RSA locales)      │
│                        │                     │
│   CONTROLLER  →  SERVICE  →  REPOSITORY      │
│     REST         Reglas       Spring Data    │
│                  de negocio      JPA         │
│                                              │
│   EXCEPTION: respuestas de error uniformes   │
└───────────────────────┬──────────────────────┘
                        │ JDBC
                        ▼
              ┌───────────────────┐
              │  MySQL / MariaDB  │
              │  XAMPP+phpMyAdmin │
              │   smartbody_db    │
              └───────────────────┘
```

## Patron MVP en la aplicacion

| Elemento | Responsabilidad | Ejemplo |
|----------|-----------------|---------|
| **View** | Dibuja la interfaz y reenvia los eventos del usuario. No decide nada. | `LoginScreen` |
| **Presenter** | Valida la entrada, llama al repositorio y publica un estado inmutable que describe exactamente que mostrar. | `LoginPresenter` + `LoginUiState` |
| **Model** | Obtiene y transforma los datos: DTO, repositorios y traduccion de errores. | `AuthRepository`, `ejecutarPeticion` |

El Presenter se apoya en `ViewModel` unicamente para conservar el estado ante rotaciones
de pantalla; la logica de presentacion no vive dentro de las funciones `@Composable`.

## Flujo de autenticacion

```text
Usuario introduce credenciales
        ↓
LoginPresenter valida el formulario
        ↓
AuthRepository → POST /api/auth/login
        ↓
AuthService busca el usuario en MySQL
        ↓
BCrypt compara la contrasena con el hash guardado
        ↓
JwtService firma un JWT con la clave privada RSA (RS256)
        ↓
La aplicacion recibe el token y lo guarda en DataStore
        ↓
AuthInterceptor anade "Authorization: Bearer <token>" a cada peticion protegida
        ↓
JwtAuthenticationFilter verifica la firma con la clave publica y autentica la peticion
```

La clave privada nunca sale del backend: la aplicacion Android solo recibe el token.

## Manejo de errores

Toda excepcion del backend pasa por `GlobalExceptionHandler` y se convierte en un JSON
uniforme con un mensaje apto para el usuario final. En la aplicacion, `ejecutarPeticion`
traduce codigos HTTP y fallos de red a mensajes como
*"No fue posible conectarse con el servidor."*, de modo que el usuario nunca ve
excepciones tecnicas.

## Preparado para el agente IA

La generacion esta implementada como un **motor de reglas** en `service/reglas`, detras
de dos interfaces:

```text
RutinaService ──────────► GeneradorRutina ◄──────── GeneradorRutinaPorReglas (MVP)
                                           ◄──────── (futuro) GeneradorRutinaConIA

NutricionService ───────► GeneradorPlanNutricional ◄─ GeneradorPlanNutricionalPorReglas (MVP)
```

Los servicios solo dependen de la interfaz. Para integrar un agente de IA basta con crear
otra implementacion que llame a la API externa **desde el backend**. Asi no se exponen
claves y no hay que modificar la aplicacion Android ni los controllers.

## Navegacion de la aplicacion

```text
Splash ─┬─► Login ─► Registro ─► Asistente de perfil
        │     │
        └─────┴─► Inicio ◄──────── barra inferior ────────┐
                   │                                      │
      ┌────────────┼──────────────┬──────────────┬────────┤
      ▼            ▼              ▼              ▼        ▼
  Generar plan   Rutina       Nutricion      Progreso   Perfil
                   │              │                       │
          Detalle ejercicio  Sustituir alimento     Editar perfil
                   │                                Cerrar sesion ─► Login
          Sustituir ejercicio
```

Si el backend responde `401` a cualquier peticion protegida (token expirado o invalido),
`AuthInterceptor` elimina el token y `TokenManager` avisa a la navegacion, que vuelve al
login borrando todo el historial.
