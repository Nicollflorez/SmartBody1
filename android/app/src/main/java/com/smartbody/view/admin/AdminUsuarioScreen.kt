package com.smartbody.view.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbody.model.dto.DetalleUsuarioAdminDto
import com.smartbody.presenter.admin.AdminUsuarioPresenter
import com.smartbody.presenter.admin.AdminUsuarioUiState
import com.smartbody.view.components.BarraSuperior
import com.smartbody.view.components.BotonPrincipal
import com.smartbody.view.components.Etiqueta
import com.smartbody.view.components.FilaDato
import com.smartbody.view.components.Formatos
import com.smartbody.view.components.GraficoPeso
import com.smartbody.view.components.PantallaCargando
import com.smartbody.view.components.PantallaError
import com.smartbody.view.components.TarjetaSeccion

/** Detalle de un usuario para el administrador: perfil, progreso, rutina y plan nutricional. */
@Composable
fun AdminUsuarioScreen(presenter: AdminUsuarioPresenter, alVolver: () -> Unit) {
    val estado by presenter.estado.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            presenter.mensajeMostrado()
        }
    }

    val detalle = estado.detalle
    if (estado.confirmandoGeneracion && detalle != null) {
        AlertDialog(
            onDismissRequest = presenter::cancelarGeneracion,
            title = { Text("Generar nuevo plan") },
            text = {
                Text(
                    "Se creará una nueva rutina y un nuevo plan nutricional para ${detalle.usuario.nombre} " +
                        "con su perfil actual. Los anteriores quedarán en su historial."
                )
            },
            confirmButton = { TextButton(onClick = presenter::generarPlan) { Text("Generar") } },
            dismissButton = { TextButton(onClick = presenter::cancelarGeneracion) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = { BarraSuperior(detalle?.usuario?.nombre ?: "Usuario", alVolver = alVolver) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->
        when {
            estado.cargando -> PantallaCargando(Modifier.padding(relleno))
            detalle == null -> PantallaError(
                mensaje = estado.errorCarga ?: "No fue posible cargar el usuario.",
                alReintentar = presenter::cargar,
                modifier = Modifier.padding(relleno)
            )

            else -> ContenidoDetalle(detalle, estado, presenter, Modifier.padding(relleno))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContenidoDetalle(
    detalle: DetalleUsuarioAdminDto,
    estado: AdminUsuarioUiState,
    presenter: AdminUsuarioPresenter,
    modifier: Modifier
) {
    val usuario = detalle.usuario
    val perfil = detalle.perfil

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TarjetaSeccion(titulo = "Cuenta") {
                FilaDato("Correo", usuario.email)
                FilaDato("Registrado", Formatos.fecha(usuario.creadoEn))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (usuario.activo) "Cuenta activa" else "Cuenta desactivada",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (usuario.activo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = usuario.activo,
                        onCheckedChange = presenter::cambiarEstado,
                        enabled = !estado.actualizandoEstado
                    )
                }
            }
        }

        item {
            TarjetaSeccion(titulo = "Perfil") {
                if (!perfil.completo) {
                    Text(
                        "El usuario aún no ha completado su perfil.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    return@TarjetaSeccion
                }
                FilaDato("Objetivo", perfil.objetivo?.nombre.orEmpty())
                FilaDato("Edad y sexo", "${perfil.edad} años · ${Formatos.legible(perfil.sexo)}")
                FilaDato("Peso y altura", "${perfil.pesoKg?.let(Formatos::numero)} kg · ${perfil.alturaCm?.let(Formatos::numero)} cm")
                FilaDato("IMC", perfil.imc?.let { "${Formatos.decimal(it)} · ${perfil.clasificacionImc.orEmpty()}" }.orEmpty())
                FilaDato("Nivel", Formatos.legible(perfil.nivelExperiencia))
                FilaDato("Disponibilidad", Formatos.diasYMinutos(perfil.diasPorSemana, perfil.minutosPorSesion))
                val restricciones = perfil.restricciones.orEmpty()
                Spacer(Modifier.height(8.dp))
                if (restricciones.isEmpty()) {
                    Text("Sin restricciones registradas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        restricciones.forEach { Etiqueta(it.nombre) }
                    }
                }
            }
        }

        item {
            TarjetaSeccion(titulo = "Progreso") {
                val progreso = detalle.progreso
                if (progreso.medidas.isEmpty()) {
                    Text("Sin medidas registradas.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    return@TarjetaSeccion
                }
                FilaDato("Peso inicial", "${Formatos.numero(progreso.pesoInicial ?: 0.0)} kg")
                FilaDato("Peso actual", "${Formatos.numero(progreso.pesoActual ?: 0.0)} kg")
                FilaDato("Cambio", progreso.cambioPesoKg?.let { (if (it > 0) "+" else "") + Formatos.numero(it) + " kg" } ?: "-")
                FilaDato("Medidas registradas", "${progreso.medidas.size}")
                Spacer(Modifier.height(8.dp))
                GraficoPeso(progreso.medidas.map { it.pesoKg })
            }
        }

        item {
            BotonPrincipal(
                texto = if (detalle.rutina == null) "Generar rutina y plan" else "Generar nueva rutina y plan",
                onClick = presenter::pedirGenerarPlan,
                cargando = estado.generando,
                habilitado = perfil.completo
            )
            if (!perfil.completo) {
                Text(
                    "Se podrá generar cuando el usuario complete su perfil.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        item { SeccionRutina(detalle, estado.diaSeleccionado, presenter::seleccionarDia) }
        item { SeccionPlan(detalle) }
    }
}

@Composable
private fun SeccionRutina(detalle: DetalleUsuarioAdminDto, diaSeleccionado: Int, alSeleccionarDia: (Int) -> Unit) {
    TarjetaSeccion(titulo = "Rutina activa") {
        val rutina = detalle.rutina
        if (rutina == null) {
            Text("El usuario aún no tiene una rutina.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@TarjetaSeccion
        }
        Text(
            "${rutina.nombre} · generada el ${Formatos.fecha(rutina.generadaEn)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            rutina.dias.forEach { d ->
                FilterChip(
                    selected = d.numero == diaSeleccionado,
                    onClick = { alSeleccionarDia(d.numero) },
                    label = { Text("Día ${d.numero}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
        val dia = rutina.dias.firstOrNull { it.numero == diaSeleccionado } ?: rutina.dias.firstOrNull() ?: return@TarjetaSeccion
        Spacer(Modifier.height(8.dp))
        Text("${dia.enfoque} · ${dia.minutosEstimados} min", style = MaterialTheme.typography.titleSmall)
        dia.ejercicios.forEachIndexed { i, e ->
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text("${i + 1}. ${e.nombre}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${Formatos.prescripcion(e.series, e.repeticiones, e.duracionSeg)} · ${Formatos.descanso(e.descansoSeg)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SeccionPlan(detalle: DetalleUsuarioAdminDto) {
    TarjetaSeccion(titulo = "Plan nutricional activo") {
        val plan = detalle.plan
        if (plan == null) {
            Text("El usuario aún no tiene un plan nutricional.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@TarjetaSeccion
        }
        Text(
            "${Formatos.entero(plan.caloriasTotales)} kcal al día · P ${Formatos.numero(plan.proteinaG)} g · " +
                "C ${Formatos.numero(plan.carbohidratosG)} g · G ${Formatos.numero(plan.grasasG)} g",
            style = MaterialTheme.typography.bodyMedium
        )
        plan.hidratacion?.let {
            Text(
                "Agua: ${Formatos.numero(it.litrosDiaEntrenamiento)} L en días de entrenamiento",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        plan.comidas.forEach { comida ->
            Spacer(Modifier.height(8.dp))
            Text("${comida.nombre} · ${comida.calorias} kcal", style = MaterialTheme.typography.titleSmall)
            Text(
                comida.alimentos.joinToString { "${it.icono.orEmpty()} ${it.nombre} (${Formatos.numero(it.cantidad)} g)" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
