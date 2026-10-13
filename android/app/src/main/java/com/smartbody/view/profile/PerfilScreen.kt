package com.smartbody.view.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import com.smartbody.view.components.AccesoRapido
import com.smartbody.view.components.AvatarUsuario
import com.smartbody.presenter.admin.PestanaAdmin
import com.smartbody.view.admin.AccesosAdministracion
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbody.presenter.profile.PerfilPresenter
import com.smartbody.view.components.AvisoSalud
import com.smartbody.view.components.BarraSuperior
import com.smartbody.view.components.BotonPrincipal
import com.smartbody.view.components.Etiqueta
import com.smartbody.view.components.FilaDato
import com.smartbody.view.components.Formatos
import com.smartbody.view.components.PantallaCargando
import com.smartbody.view.components.TarjetaDestacada
import com.smartbody.view.components.TarjetaSeccion

/** Perfil del usuario y cierre de sesion (RF12). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PerfilScreen(
    presenter: PerfilPresenter,
    barraInferior: @Composable () -> Unit,
    alEditarPerfil: () -> Unit,
    alCerrarSesion: () -> Unit,
    alAbrirAdministracion: (PestanaAdmin) -> Unit = {}
) {
    val estado by presenter.estado.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { presenter.cargar() }

    LaunchedEffect(estado.sesionCerrada) {
        if (estado.sesionCerrada) alCerrarSesion()
    }

    LaunchedEffect(estado.mensajeError, estado.mensajeExito) {
        (estado.mensajeExito ?: estado.mensajeError)?.let {
            snackbarHostState.showSnackbar(it)
            presenter.mensajesMostrados()
        }
    }

    // Selector de fotos del sistema: no requiere permisos de almacenamiento.
    val selectorFoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) presenter.alSeleccionarFoto(uri)
    }
    val elegirFoto = {
        selectorFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    if (estado.confirmandoCierre) {
        AlertDialog(
            onDismissRequest = presenter::cancelarCierre,
            title = { Text("Cerrar sesión") },
            text = { Text("Tendrás que ingresar tu correo y contraseña para volver a entrar.") },
            confirmButton = { TextButton(onClick = presenter::cerrarSesion) { Text("Cerrar sesión") } },
            dismissButton = { TextButton(onClick = presenter::cancelarCierre) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = { BarraSuperior("Perfil") },
        bottomBar = barraInferior,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->
        if (estado.cargando) {
            PantallaCargando(Modifier.padding(relleno))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AvatarUsuario(
                        foto = estado.foto,
                        nombre = estado.nombre,
                        tamano = 112.dp,
                        modifier = Modifier.clickable(enabled = !estado.procesandoFoto) { elegirFoto() }
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(36.dp)
                            .clickable(enabled = !estado.procesandoFoto) { elegirFoto() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (estado.procesandoFoto) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    Icons.Filled.PhotoCamera,
                                    contentDescription = "Cambiar foto de perfil",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(estado.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(estado.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row {
                    TextButton(onClick = elegirFoto, enabled = !estado.procesandoFoto) {
                        Text(if (estado.foto == null) "Agregar foto" else "Cambiar foto")
                    }
                    if (estado.foto != null) {
                        TextButton(
                            onClick = presenter::quitarFoto,
                            enabled = !estado.procesandoFoto,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text("Quitar foto")
                        }
                    }
                }
            }

            if (estado.esAdministrador) {
                AccesosAdministracion(alAbrir = alAbrirAdministracion)
            }

            val perfil = estado.perfil
            if (perfil == null || !perfil.completo) {
                TarjetaDestacada(
                    icono = Icons.Filled.PersonAdd,
                    titulo = "Tu perfil está incompleto",
                    mensaje = "Completa tus datos para recibir una rutina y un plan nutricional personalizados.",
                    textoBoton = "Completar perfil",
                    alPulsar = alEditarPerfil
                )
            } else {
                TarjetaSeccion(titulo = "Datos físicos") {
                    FilaDato("Edad", "${perfil.edad} años")
                    FilaDato("Sexo", Formatos.legible(perfil.sexo))
                    FilaDato("Peso", perfil.pesoKg?.let { "${Formatos.numero(it)} kg" }.orEmpty())
                    FilaDato("Altura", perfil.alturaCm?.let { "${Formatos.numero(it)} cm" }.orEmpty())
                    FilaDato(
                        "IMC",
                        perfil.imc?.let { "${Formatos.decimal(it)} - ${perfil.clasificacionImc.orEmpty()}" }.orEmpty()
                    )
                }

                TarjetaSeccion(titulo = "Entrenamiento") {
                    FilaDato("Objetivo", perfil.objetivo?.nombre.orEmpty())
                    FilaDato("Nivel", Formatos.legible(perfil.nivelExperiencia))
                    FilaDato("Disponibilidad", Formatos.diasYMinutos(perfil.diasPorSemana, perfil.minutosPorSesion))
                }

                TarjetaSeccion(titulo = "Restricciones") {
                    val restricciones = perfil.restricciones.orEmpty()
                    if (restricciones.isEmpty()) {
                        Text(
                            "No registraste restricciones de salud ni de alimentación.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            restricciones.forEach { Etiqueta(it.nombre) }
                        }
                    }
                }

                BotonPrincipal(texto = "Editar perfil", onClick = alEditarPerfil)
            }

            OutlinedButton(
                onClick = presenter::pedirCerrarSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }

            AvisoSalud()
            Spacer(Modifier.height(8.dp))
        }
    }
}
