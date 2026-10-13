package com.smartbody.navigation

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smartbody.presenter.admin.AdminPresenter
import com.smartbody.presenter.admin.PestanaAdmin
import com.smartbody.presenter.admin.AdminUsuarioPresenter
import com.smartbody.presenter.chat.ChatPresenter
import com.smartbody.view.admin.AdminScreen
import com.smartbody.view.admin.AdminUsuarioScreen
import com.smartbody.view.chat.ChatScreen
import androidx.navigation.navArgument
import com.smartbody.contenedor
import com.smartbody.presenter.dashboard.DashboardPresenter
import com.smartbody.presenter.login.LoginPresenter
import com.smartbody.presenter.nutrition.NutricionPresenter
import com.smartbody.presenter.nutrition.SustituirAlimentoPresenter
import com.smartbody.presenter.plan.GenerarPlanPresenter
import com.smartbody.presenter.profile.PerfilFormPresenter
import com.smartbody.presenter.profile.PerfilPresenter
import com.smartbody.presenter.progress.ProgresoPresenter
import com.smartbody.presenter.register.RegisterPresenter
import com.smartbody.presenter.routine.DetalleEjercicioPresenter
import com.smartbody.presenter.routine.RutinaPresenter
import com.smartbody.presenter.routine.SustituirEjercicioPresenter
import com.smartbody.presenter.splash.SplashPresenter
import com.smartbody.view.components.BarraInferior
import com.smartbody.view.dashboard.DashboardScreen
import com.smartbody.view.login.LoginScreen
import com.smartbody.view.nutrition.NutricionScreen
import com.smartbody.view.nutrition.SustituirAlimentoScreen
import com.smartbody.view.plan.GenerarPlanScreen
import com.smartbody.view.profile.EditarPerfilScreen
import com.smartbody.view.profile.PerfilScreen
import com.smartbody.view.progress.ProgresoScreen
import com.smartbody.view.register.RegisterScreen
import com.smartbody.view.routine.DetalleEjercicioScreen
import com.smartbody.view.routine.RutinaScreen
import com.smartbody.view.routine.SustituirEjercicioScreen
import com.smartbody.view.splash.SplashScreen

/** Rutas de navegacion de la aplicacion. */
object Rutas {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTRO = "registro"

    // Pestanas de la barra inferior (requieren sesion)
    const val INICIO = "inicio"
    const val RUTINA = "rutina"
    const val NUTRICION = "nutricion"
    const val PROGRESO = "progreso"
    const val PERFIL = "perfil"

    const val EDITAR_PERFIL = "perfil/editar"
    const val GENERAR_PLAN = "plan/generar"
    const val DETALLE_EJERCICIO = "rutina/{rutinaId}/ejercicio/{itemId}"
    const val SUSTITUIR_EJERCICIO = "rutina/{rutinaId}/sustituir/{itemId}"
    const val SUSTITUIR_ALIMENTO = "nutricion/{planId}/sustituir/{itemId}"
    const val CHAT = "chat"
    const val ADMINISTRACION = "administracion?pestana={pestana}"
    const val ADMIN_USUARIO = "administracion/usuario/{usuarioId}"

    fun adminUsuario(usuarioId: Long) = "administracion/usuario/$usuarioId"
    fun administracion(pestana: PestanaAdmin) = "administracion?pestana=${pestana.name}"

    /** Pantallas principales donde se muestra la barra inferior y el boton del chat. */
    val PESTANAS = setOf(INICIO, RUTINA, NUTRICION, PROGRESO, PERFIL)

    fun detalleEjercicio(rutinaId: Long, itemId: Long) = "rutina/$rutinaId/ejercicio/$itemId"
    fun sustituirEjercicio(rutinaId: Long, itemId: Long) = "rutina/$rutinaId/sustituir/$itemId"
    fun sustituirAlimento(planId: Long, itemId: Long) = "nutricion/$planId/sustituir/$itemId"
}

@Composable
fun NavegacionSmartbody() {
    val navController = rememberNavController()
    val tokenManager = LocalContext.current.contenedor.tokenManager

    // Token expirado o invalido en cualquier pantalla: se vuelve al login sin historial (RNF06).
    LaunchedEffect(Unit) {
        tokenManager.sesionExpirada.collect { navController.irAlLogin() }
    }

    val barra: @Composable (String) -> Unit = { rutaActual ->
        BarraInferior(rutaActual = rutaActual, alNavegar = { navController.irAPestana(it) })
    }

    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = Rutas.SPLASH) {

            composable(Rutas.SPLASH) {
                val presenter = recordarPresenter { SplashPresenter(it.authRepository) }
                SplashScreen(
                    presenter = presenter,
                    alIrALogin = {
                        navController.navigate(Rutas.LOGIN) { popUpTo(Rutas.SPLASH) { inclusive = true } }
                    },
                    alIrADashboard = {
                        navController.navigate(Rutas.INICIO) { popUpTo(Rutas.SPLASH) { inclusive = true } }
                    }
                )
            }

            composable(Rutas.LOGIN) {
                val presenter = recordarPresenter { LoginPresenter(it.authRepository) }
                LoginScreen(
                    presenter = presenter,
                    alIniciarSesion = {
                        // Todos entran a la aplicacion; el administrador ademas ve el acceso a su panel.
                        // Las pantallas protegidas no deben poder alcanzarse con el boton atras.
                        navController.navigate(Rutas.INICIO) { popUpTo(Rutas.LOGIN) { inclusive = true } }
                    },
                    alIrARegistro = { navController.navigate(Rutas.REGISTRO) }
                )
            }

            composable(Rutas.REGISTRO) {
                val presenter = recordarPresenter { RegisterPresenter(it.authRepository) }
                RegisterScreen(
                    presenter = presenter,
                    alRegistrarse = {
                        // Tras crear la cuenta se guia al usuario a completar su perfil.
                        navController.navigate(Rutas.INICIO) { popUpTo(Rutas.LOGIN) { inclusive = true } }
                        navController.navigate(Rutas.EDITAR_PERFIL)
                    },
                    alVolver = { navController.popBackStack() }
                )
            }

            composable(Rutas.INICIO) {
                val presenter = recordarPresenter { DashboardPresenter(it.perfilRepository, it.notificadorRutina) }
                DashboardScreen(
                    presenter = presenter,
                    barraInferior = { barra(Rutas.INICIO) },
                    alCompletarPerfil = { navController.navigate(Rutas.EDITAR_PERFIL) },
                    alGenerarPlan = { navController.navigate(Rutas.GENERAR_PLAN) },
                    alIrAPestana = { navController.irAPestana(it) },
                    alAbrirAdministracion = { navController.navigate(Rutas.administracion(it)) }
                )
            }

            composable(Rutas.RUTINA) {
                val presenter = recordarPresenter { RutinaPresenter(it.rutinaRepository) }
                RutinaScreen(
                    presenter = presenter,
                    barraInferior = { barra(Rutas.RUTINA) },
                    alVerEjercicio = { rutinaId, itemId -> navController.navigate(Rutas.detalleEjercicio(rutinaId, itemId)) },
                    alGenerarPlan = { navController.navigate(Rutas.GENERAR_PLAN) }
                )
            }

            composable(Rutas.NUTRICION) {
                val presenter = recordarPresenter { NutricionPresenter(it.nutricionRepository) }
                NutricionScreen(
                    presenter = presenter,
                    barraInferior = { barra(Rutas.NUTRICION) },
                    alSustituir = { planId, itemId -> navController.navigate(Rutas.sustituirAlimento(planId, itemId)) },
                    alGenerarPlan = { navController.navigate(Rutas.GENERAR_PLAN) }
                )
            }

            composable(Rutas.PROGRESO) {
                val presenter = recordarPresenter { ProgresoPresenter(it.progresoRepository) }
                ProgresoScreen(presenter = presenter, barraInferior = { barra(Rutas.PROGRESO) })
            }

            composable(Rutas.PERFIL) {
                val presenter = recordarPresenter { PerfilPresenter(it.perfilRepository, it.authRepository) }
                PerfilScreen(
                    presenter = presenter,
                    barraInferior = { barra(Rutas.PERFIL) },
                    alEditarPerfil = { navController.navigate(Rutas.EDITAR_PERFIL) },
                    alCerrarSesion = { navController.irAlLogin() },
                    alAbrirAdministracion = { navController.navigate(Rutas.administracion(it)) }
                )
            }

            composable(Rutas.EDITAR_PERFIL) {
                val presenter = recordarPresenter { PerfilFormPresenter(it.perfilRepository) }
                EditarPerfilScreen(
                    presenter = presenter,
                    alGuardar = { navController.popBackStack() },
                    alVolver = { navController.popBackStack() }
                )
            }

            composable(Rutas.GENERAR_PLAN) {
                val presenter = recordarPresenter {
                    GenerarPlanPresenter(it.perfilRepository, it.rutinaRepository, it.nutricionRepository, it.notificadorRutina)
                }
                GenerarPlanScreen(
                    presenter = presenter,
                    alGenerar = {
                        navController.navigate(Rutas.RUTINA) {
                            popUpTo(Rutas.INICIO)
                            launchSingleTop = true
                        }
                    },
                    alCompletarPerfil = {
                        navController.navigate(Rutas.EDITAR_PERFIL) { popUpTo(Rutas.INICIO) }
                    },
                    alVolver = { navController.popBackStack() }
                )
            }

            composable(
                Rutas.DETALLE_EJERCICIO,
                arguments = listOf(
                    navArgument("rutinaId") { type = NavType.LongType },
                    navArgument("itemId") { type = NavType.LongType }
                )
            ) { entrada ->
                val rutinaId = entrada.arguments?.getLong("rutinaId") ?: 0L
                val itemId = entrada.arguments?.getLong("itemId") ?: 0L
                val presenter = recordarPresenter { DetalleEjercicioPresenter(it.rutinaRepository, rutinaId, itemId) }
                DetalleEjercicioScreen(
                    presenter = presenter,
                    alSustituir = { navController.navigate(Rutas.sustituirEjercicio(rutinaId, itemId)) },
                    alVolver = { navController.popBackStack() }
                )
            }

            composable(
                Rutas.SUSTITUIR_EJERCICIO,
                arguments = listOf(
                    navArgument("rutinaId") { type = NavType.LongType },
                    navArgument("itemId") { type = NavType.LongType }
                )
            ) { entrada ->
                val rutinaId = entrada.arguments?.getLong("rutinaId") ?: 0L
                val itemId = entrada.arguments?.getLong("itemId") ?: 0L
                val presenter = recordarPresenter { SustituirEjercicioPresenter(it.rutinaRepository, rutinaId, itemId) }
                SustituirEjercicioScreen(
                    presenter = presenter,
                    alTerminar = {
                        // Vuelve a la rutina, que se recarga con el ejercicio nuevo.
                        if (!navController.popBackStack(Rutas.RUTINA, inclusive = false)) navController.popBackStack()
                    },
                    alVolver = { navController.popBackStack() }
                )
            }

            composable(
                Rutas.SUSTITUIR_ALIMENTO,
                arguments = listOf(
                    navArgument("planId") { type = NavType.LongType },
                    navArgument("itemId") { type = NavType.LongType }
                )
            ) { entrada ->
                val planId = entrada.arguments?.getLong("planId") ?: 0L
                val itemId = entrada.arguments?.getLong("itemId") ?: 0L
                val presenter = recordarPresenter { SustituirAlimentoPresenter(it.nutricionRepository, planId, itemId) }
                SustituirAlimentoScreen(
                    presenter = presenter,
                    alTerminar = { navController.popBackStack() },
                    alVolver = { navController.popBackStack() }
                )
            }

            composable(
                Rutas.ADMINISTRACION,
                arguments = listOf(navArgument("pestana") { type = NavType.StringType; defaultValue = PestanaAdmin.RESUMEN.name })
            ) { entrada ->
                val pestana = PestanaAdmin.entries.firstOrNull { it.name == entrada.arguments?.getString("pestana") }
                    ?: PestanaAdmin.RESUMEN
                val presenter = recordarPresenter { AdminPresenter(it.adminRepository, pestana) }
                AdminScreen(
                    presenter = presenter,
                    alVerUsuario = { id -> navController.navigate(Rutas.adminUsuario(id)) },
                    alVolver = { navController.popBackStack() }
                )
            }

            composable(
                Rutas.ADMIN_USUARIO,
                arguments = listOf(navArgument("usuarioId") { type = NavType.LongType })
            ) { entrada ->
                val usuarioId = entrada.arguments?.getLong("usuarioId") ?: 0L
                val presenter = recordarPresenter { AdminUsuarioPresenter(it.adminRepository, usuarioId) }
                AdminUsuarioScreen(presenter = presenter, alVolver = { navController.popBackStack() })
            }

            composable(Rutas.CHAT) {
                val presenter = recordarPresenter { ChatPresenter(it.chatRepository, it.authRepository) }
                ChatScreen(presenter = presenter, alVolver = { navController.popBackStack() })
            }
        }

        // Boton flotante del chatbot, a un lado de las pantallas principales.
        if (rutaActual in Rutas.PESTANAS) {
            FloatingActionButton(
                onClick = { navController.navigate(Rutas.CHAT) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    // Encima de la barra inferior (80 dp) y de la barra de navegacion del sistema.
                    .navigationBarsPadding()
                    .padding(end = 16.dp, bottom = 100.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Abrir chat con el asistente")
            }
        }
    }
}

/** Cambia de pestana conservando el estado de cada una y sin acumular historial. */
private fun NavController.irAPestana(ruta: String) {
    if (ruta == Rutas.INICIO) {
        // Inicio es la base de la pila: se vuelve a ella directamente. Navegar con restoreState
        // restauraria la ultima pestana guardada bajo Inicio y el usuario no podria regresar.
        if (!popBackStack(Rutas.INICIO, inclusive = false)) {
            navigate(Rutas.INICIO) { launchSingleTop = true }
        }
        return
    }
    navigate(ruta) {
        popUpTo(Rutas.INICIO) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** RF12 - al cerrar sesion (o si expira) se limpia toda la pila de navegacion. */
private fun NavController.irAlLogin() {
    navigate(Rutas.LOGIN) { popUpTo(0) { inclusive = true } }
}
