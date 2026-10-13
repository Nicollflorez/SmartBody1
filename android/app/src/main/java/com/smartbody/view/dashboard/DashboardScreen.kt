package com.smartbody.view.dashboard

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbody.model.dto.ResumenDto
import com.smartbody.navigation.Rutas
import com.smartbody.presenter.dashboard.DashboardPresenter
import com.smartbody.view.components.AccesoRapido
import com.smartbody.view.components.AvatarUsuario
import com.smartbody.view.components.AvisoSalud
import com.smartbody.view.components.DatoResumen
import com.smartbody.view.components.Formatos
import com.smartbody.view.components.LogoSmartbody
import com.smartbody.view.components.PantallaCargando
import com.smartbody.presenter.admin.PestanaAdmin
import com.smartbody.view.admin.AccesosAdministracion
import com.smartbody.view.components.PantallaError
import com.smartbody.view.components.TarjetaDestacada
import com.smartbody.view.components.TarjetaSeccion

/** Pantalla de inicio. Solo es accesible con un JWT valido. */
@Composable
fun DashboardScreen(
    presenter: DashboardPresenter,
    barraInferior: @Composable () -> Unit,
    alCompletarPerfil: () -> Unit,
    alGenerarPlan: () -> Unit,
    alIrAPestana: (String) -> Unit,
    alAbrirAdministracion: (PestanaAdmin) -> Unit
) {
    val estado by presenter.estado.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { presenter.cargar() }

    // RF11: permiso de notificaciones (Android 13+). Se pide una sola vez y solo si hay una rutina que
    // recordar; si el usuario lo rechaza la aplicacion sigue igual y el aviso se ve solo en esta pantalla.
    val solicitarPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(estado.tieneRutina) {
        if (estado.tieneRutina && presenter.debeSolicitarPermisoNotificaciones()) {
            presenter.permisoNotificacionesSolicitado()
            solicitarPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(estado.mensajeError, estado.resumen) {
        val mensaje = estado.mensajeError
        if (mensaje != null && estado.resumen != null) {
            snackbarHostState.showSnackbar(mensaje)
            presenter.mensajeMostrado()
        }
    }

    Scaffold(
        bottomBar = barraInferior,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->
        val resumen = estado.resumen
        when {
            estado.cargando -> PantallaCargando(Modifier.padding(relleno))
            resumen == null -> PantallaError(
                mensaje = estado.mensajeError ?: "No fue posible cargar tu información.",
                alReintentar = presenter::cargar,
                modifier = Modifier.padding(relleno)
            )

            else -> ContenidoInicio(
                resumen = resumen,
                foto = estado.foto,
                modifier = Modifier.padding(relleno),
                alCompletarPerfil = alCompletarPerfil,
                alGenerarPlan = alGenerarPlan,
                alIrAPestana = alIrAPestana,
                alAbrirAdministracion = alAbrirAdministracion
            )
        }
    }
}

@Composable
private fun ContenidoInicio(
    resumen: ResumenDto,
    foto: ByteArray?,
    modifier: Modifier,
    alCompletarPerfil: () -> Unit,
    alGenerarPlan: () -> Unit,
    alIrAPestana: (String) -> Unit,
    alAbrirAdministracion: (PestanaAdmin) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (foto != null) AvatarUsuario(foto, resumen.nombre, 56.dp) else LogoSmartbody(tamano = 56)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Hola, ${resumen.nombre.substringBefore(' ')}", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Entrena, mejora y avanza",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // El administrador usa la aplicacion como cualquier usuario y ademas tiene acceso a su panel.
        if (resumen.esAdministrador) {
            AccesosAdministracion(alAbrir = alAbrirAdministracion)
        }

        if (!resumen.perfilCompleto) {
            TarjetaDestacada(
                icono = Icons.Filled.PersonAdd,
                titulo = "Completa tu perfil",
                mensaje = "Cuéntanos tus datos físicos, tu objetivo, tu disponibilidad y tus restricciones para crear un plan a tu medida.",
                textoBoton = "Completar perfil",
                alPulsar = alCompletarPerfil
            )
            AvisoSalud()
            return@Column
        }

        TarjetaSeccion(titulo = "Tu perfil") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                DatoResumen("Objetivo", resumen.objetivo.orEmpty(), Modifier.weight(1.4f))
                DatoResumen("Peso", resumen.pesoKg?.let { "${Formatos.numero(it)} kg" } ?: "-", Modifier.weight(1f))
                DatoResumen(
                    etiqueta = resumen.clasificacionImc ?: "IMC",
                    valor = resumen.imc?.let { "IMC ${Formatos.decimal(it)}" } ?: "-",
                    modifier = Modifier.weight(1.2f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                Formatos.diasYMinutos(resumen.diasPorSemana, resumen.minutosPorSesion),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        // RF11: aviso dentro de la aplicacion para renovar la rutina.
        if (resumen.sugerirCambioRutina) {
            TarjetaDestacada(
                icono = Icons.Filled.NotificationsActive,
                titulo = "Es momento de renovar tu rutina",
                mensaje = "Llevas ${resumen.diasDesdeRutina} días con la misma rutina. Un plan nuevo ayuda a seguir progresando.",
                textoBoton = "Generar nueva rutina",
                alPulsar = alGenerarPlan
            )
        }

        if (resumen.rutinaActivaId == null) {
            TarjetaDestacada(
                icono = Icons.Filled.AutoAwesome,
                titulo = "Genera tu plan personalizado",
                mensaje = "Con tu perfil completo ya podemos crear tu rutina de entrenamiento y tu plan nutricional.",
                textoBoton = "Generar mi plan",
                alPulsar = alGenerarPlan
            )
        } else {
            AccesoRapido(
                icono = Icons.Filled.FitnessCenter,
                titulo = "Mi rutina",
                subtitulo = "Ejercicios de cada día de entrenamiento",
                alPulsar = { alIrAPestana(Rutas.RUTINA) }
            )
            AccesoRapido(
                icono = Icons.Filled.Restaurant,
                titulo = "Mi plan nutricional",
                subtitulo = if (resumen.planActivoId != null) "Comidas y cantidades recomendadas" else "Aún no generado",
                alPulsar = { alIrAPestana(Rutas.NUTRICION) }
            )
        }

        AccesoRapido(
            icono = Icons.Filled.MonitorWeight,
            titulo = "Registrar medidas",
            subtitulo = "Actualiza tu peso y sigue tu progreso",
            alPulsar = { alIrAPestana(Rutas.PROGRESO) }
        )

        AvisoSalud()
    }
}
