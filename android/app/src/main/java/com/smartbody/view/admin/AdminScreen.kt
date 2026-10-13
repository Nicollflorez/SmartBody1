package com.smartbody.view.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbody.model.dto.ActividadDto
import com.smartbody.model.dto.ConteoDto
import com.smartbody.model.dto.ProgresoUsuarioDto
import com.smartbody.model.dto.ReporteEfectividadDto
import com.smartbody.model.dto.ResumenAdminDto
import com.smartbody.model.dto.UsuarioAdminDto
import com.smartbody.presenter.admin.AdminPresenter
import com.smartbody.presenter.admin.AdminUiState
import com.smartbody.presenter.admin.PestanaAdmin
import com.smartbody.view.components.BarraSuperior
import com.smartbody.view.components.DatoResumen
import com.smartbody.view.components.Etiqueta
import com.smartbody.view.components.Formatos
import com.smartbody.view.components.PantallaCargando
import com.smartbody.view.components.PantallaError
import com.smartbody.view.components.TarjetaSeccion

/** Panel de administracion: resumen e historial de uso, usuarios y reportes de efectividad. */
@Composable
fun AdminScreen(
    presenter: AdminPresenter,
    alVerUsuario: (Long) -> Unit,
    alVolver: () -> Unit
) {
    val estado by presenter.estado.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { presenter.cargar() }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            presenter.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior("Administración", alVolver = alVolver, acciones = {
                IconButton(onClick = presenter::cargar) { Icon(Icons.Filled.Refresh, contentDescription = "Actualizar") }
            })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->
        val resumen = estado.resumen
        when {
            estado.cargando -> PantallaCargando(Modifier.padding(relleno))
            estado.errorCarga != null || resumen == null -> PantallaError(
                mensaje = estado.errorCarga ?: "No fue posible cargar el panel.",
                alReintentar = presenter::cargar,
                modifier = Modifier.padding(relleno)
            )

            else -> Column(modifier = Modifier.fillMaxSize().padding(relleno)) {
                ScrollableTabRow(
                    selectedTabIndex = estado.pestana.ordinal,
                    containerColor = MaterialTheme.colorScheme.background,
                    edgePadding = 12.dp
                ) {
                    Tab(
                        selected = estado.pestana == PestanaAdmin.RESUMEN,
                        onClick = { presenter.seleccionarPestana(PestanaAdmin.RESUMEN) },
                        text = { Text("Resumen") }
                    )
                    Tab(
                        selected = estado.pestana == PestanaAdmin.USUARIOS,
                        onClick = { presenter.seleccionarPestana(PestanaAdmin.USUARIOS) },
                        text = { Text("Usuarios (${estado.usuarios.count { !it.administrador }})") }
                    )
                    Tab(
                        selected = estado.pestana == PestanaAdmin.REPORTES,
                        onClick = { presenter.seleccionarPestana(PestanaAdmin.REPORTES) },
                        text = { Text("Reportes") }
                    )
                }
                when (estado.pestana) {
                    PestanaAdmin.RESUMEN -> ContenidoResumen(resumen)
                    PestanaAdmin.USUARIOS -> ContenidoUsuarios(estado, presenter, alVerUsuario)
                    PestanaAdmin.REPORTES -> ContenidoReportes(estado.reporte, alVerUsuario)
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// Resumen e historial de uso
// ------------------------------------------------------------------

@Composable
private fun ContenidoResumen(resumen: ResumenAdminDto) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TarjetaSeccion(titulo = "Usuarios") {
                Row(modifier = Modifier.fillMaxWidth()) {
                    DatoResumen("Registrados", "${resumen.totalUsuarios}", Modifier.weight(1f))
                    DatoResumen("Activos", "${resumen.usuariosActivos}", Modifier.weight(1f))
                    DatoResumen("Nuevos (30 días)", "${resumen.usuariosNuevosUltimos30Dias}", Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    DatoResumen("Perfiles completos", "${resumen.perfilesCompletos}", Modifier.weight(1f))
                    DatoResumen("Sin perfil", "${resumen.totalUsuarios - resumen.perfilesCompletos}", Modifier.weight(1f))
                }
            }
        }
        item {
            TarjetaSeccion(titulo = "Uso de la aplicación") {
                Row(modifier = Modifier.fillMaxWidth()) {
                    DatoResumen("Rutinas generadas", "${resumen.rutinasGeneradas}", Modifier.weight(1f))
                    DatoResumen("Planes nutricionales", "${resumen.planesGenerados}", Modifier.weight(1f))
                    DatoResumen("Medidas registradas", "${resumen.medidasRegistradas}", Modifier.weight(1f))
                }
            }
        }
        item { TarjetaConteos("Objetivos más elegidos", resumen.usuariosPorObjetivo, "Aún no hay perfiles con objetivo.") }
        item { TarjetaConteos("Restricciones más comunes", resumen.restriccionesMasComunes, "Nadie ha registrado restricciones.") }
        item { Text("Historial de uso reciente", style = MaterialTheme.typography.headlineSmall) }
        if (resumen.actividadReciente.isEmpty()) {
            item { Text("Todavía no hay actividad.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        items(resumen.actividadReciente) { FilaActividad(it) }
    }
}

@Composable
private fun TarjetaConteos(titulo: String, conteos: List<ConteoDto>, vacio: String) {
    TarjetaSeccion(titulo = titulo) {
        if (conteos.isEmpty()) {
            Text(vacio, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@TarjetaSeccion
        }
        val maximo = conteos.maxOf { it.cantidad }.coerceAtLeast(1)
        conteos.forEach { conteo ->
            BarraConEtiqueta(conteo.nombre, "${conteo.cantidad}", conteo.cantidad.toFloat() / maximo)
        }
    }
}

@Composable
private fun BarraConEtiqueta(etiqueta: String, valor: String, proporcion: Float) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
    LinearProgressIndicator(
        progress = { proporcion.coerceIn(0f, 1f) },
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun FilaActividad(actividad: ActividadDto) {
    val icono: ImageVector = when (actividad.tipo) {
        "REGISTRO" -> Icons.Filled.PersonAdd
        "RUTINA" -> Icons.Filled.FitnessCenter
        "PLAN" -> Icons.Filled.Restaurant
        else -> Icons.Filled.MonitorWeight
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(actividad.usuario, style = MaterialTheme.typography.titleSmall)
            Text(actividad.descripcion, style = MaterialTheme.typography.bodyMedium)
            Text(
                Formatos.fechaHora(actividad.fecha),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ------------------------------------------------------------------
// Usuarios
// ------------------------------------------------------------------

@Composable
private fun ContenidoUsuarios(estado: AdminUiState, presenter: AdminPresenter, alVerUsuario: (Long) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Toca un usuario para ver su perfil, progreso, rutina y plan nutricional. " +
                    "Una cuenta desactivada no puede iniciar sesión.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(estado.usuarios.filter { !it.administrador }, key = { it.id }) { usuario ->
            TarjetaUsuario(
                usuario = usuario,
                actualizando = estado.usuarioActualizando == usuario.id,
                alCambiarEstado = { activo -> presenter.cambiarEstado(usuario, activo) },
                alVer = { alVerUsuario(usuario.id) }
            )
        }
    }
}

@Composable
private fun TarjetaUsuario(
    usuario: UsuarioAdminDto,
    actualizando: Boolean,
    alCambiarEstado: (Boolean) -> Unit,
    alVer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = alVer),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(usuario.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(usuario.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = usuario.activo, onCheckedChange = alCambiarEstado, enabled = !actualizando)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        listOfNotNull(
                            if (usuario.activo) "Activa" else "Desactivada",
                            if (usuario.perfilCompleto) usuario.objetivo ?: "Perfil completo" else "Perfil incompleto"
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (usuario.activo) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                    )
                    Text(
                        "${usuario.rutinas} rutinas · ${usuario.medidas} medidas · desde ${Formatos.fecha(usuario.creadoEn)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Ver detalle de ${usuario.nombre}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ------------------------------------------------------------------
// Reportes de efectividad
// ------------------------------------------------------------------

@Composable
private fun ContenidoReportes(reporte: ReporteEfectividadDto?, alVerUsuario: (Long) -> Unit) {
    if (reporte == null) {
        PantallaCargando()
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TarjetaSeccion(titulo = "Efectividad de Smartbody") {
                if (reporte.usuariosConSeguimiento == 0L) {
                    Text(
                        "Aún no hay usuarios con al menos dos medidas registradas para medir su progreso.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    return@TarjetaSeccion
                }
                Text(
                    "${Formatos.numero(reporte.porcentajeMejora)} %",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "de los usuarios con seguimiento avanza hacia su objetivo " +
                        "(${reporte.usuariosQueMejoraron} de ${reporte.usuariosConSeguimiento}).",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    DatoResumen("Con perfil completo", "${reporte.usuariosConPerfil}", Modifier.weight(1f))
                    DatoResumen("Con seguimiento", "${reporte.usuariosConSeguimiento}", Modifier.weight(1f))
                    DatoResumen("Días de seguimiento", Formatos.numero(reporte.promedioDiasSeguimiento), Modifier.weight(1f))
                }
            }
        }
        if (reporte.porObjetivo.isNotEmpty()) {
            item {
                TarjetaSeccion(titulo = "Por objetivo") {
                    reporte.porObjetivo.forEach { o ->
                        BarraConEtiqueta(
                            etiqueta = "${o.objetivo} (${o.mejoraron} de ${o.usuarios})",
                            valor = "${Formatos.numero(o.porcentajeMejora)} %",
                            proporcion = (o.porcentajeMejora / 100).toFloat()
                        )
                        Text(
                            "Cambio promedio de peso: ${signo(o.promedioCambioPesoKg)} kg",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
        if (reporte.usuarios.isNotEmpty()) {
            item { Text("Progreso por usuario", style = MaterialTheme.typography.headlineSmall) }
            items(reporte.usuarios, key = { it.id }) { FilaProgreso(it) { alVerUsuario(it.id) } }
        }
        item {
            Text(
                "Cómo se mide: perder peso = bajar de peso; ganar masa = subir de peso; mantener = variar máximo 1 kg; " +
                    "condición física y resistencia = IMC más cerca del rango saludable. Se compara la primera y la última medida.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilaProgreso(progreso: ProgresoUsuarioDto, alPulsar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = alPulsar),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (progreso.cambioPesoKg < 0) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = if (progreso.mejoro) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(progreso.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${progreso.objetivo} · ${Formatos.numero(progreso.pesoInicial)} → ${Formatos.numero(progreso.pesoActual)} kg " +
                        "(${signo(progreso.cambioPesoKg)} kg) en ${progreso.diasSeguimiento} días",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Etiqueta(if (progreso.mejoro) "Avanza" else "Sin avance")
        }
    }
}

private fun signo(valor: Double): String = if (valor > 0) "+${Formatos.numero(valor)}" else Formatos.numero(valor)
