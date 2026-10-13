package com.smartbody.view.routine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbody.model.dto.ItemRutinaDto
import com.smartbody.model.dto.RutinaDto
import com.smartbody.presenter.routine.RutinaPresenter
import com.smartbody.view.components.AvisoSalud
import com.smartbody.view.components.BarraSuperior
import com.smartbody.view.components.EstadoVacio
import com.smartbody.view.components.Formatos
import com.smartbody.view.components.PantallaCargando
import com.smartbody.view.components.PantallaError
import com.smartbody.view.components.TarjetaSeccion

/** RF07 - rutina vigente organizada por dias. */
@Composable
fun RutinaScreen(
    presenter: RutinaPresenter,
    barraInferior: @Composable () -> Unit,
    alVerEjercicio: (rutinaId: Long, itemId: Long) -> Unit,
    alGenerarPlan: () -> Unit
) {
    val estado by presenter.estado.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { presenter.cargar() }

    LaunchedEffect(estado.mensajeError, estado.rutina) {
        val mensaje = estado.mensajeError
        if (mensaje != null && estado.rutina != null) {
            snackbarHostState.showSnackbar(mensaje)
            presenter.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior(titulo = "Mi rutina", acciones = {
                if (estado.rutina != null) {
                    IconButton(onClick = alGenerarPlan) {
                        Icon(Icons.Filled.Autorenew, contentDescription = "Generar una nueva rutina")
                    }
                }
            })
        },
        bottomBar = barraInferior,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->
        val rutina = estado.rutina
        when {
            estado.cargando -> PantallaCargando(Modifier.padding(relleno))
            estado.sinRutina -> EstadoVacio(
                icono = Icons.Filled.FitnessCenter,
                titulo = "Aún no tienes una rutina",
                mensaje = "Genera tu plan y aquí verás los ejercicios de cada día de entrenamiento.",
                textoBoton = "Generar mi plan",
                alPulsar = alGenerarPlan,
                modifier = Modifier.padding(relleno)
            )

            rutina == null -> PantallaError(
                mensaje = estado.mensajeError ?: "No fue posible cargar tu rutina.",
                alReintentar = presenter::cargar,
                modifier = Modifier.padding(relleno)
            )

            else -> ContenidoRutina(
                rutina = rutina,
                diaSeleccionado = estado.diaSeleccionado,
                relleno = relleno,
                alSeleccionarDia = presenter::seleccionarDia,
                alVerEjercicio = { itemId -> alVerEjercicio(rutina.id, itemId) }
            )
        }
    }
}

@Composable
private fun ContenidoRutina(
    rutina: RutinaDto,
    diaSeleccionado: Int,
    relleno: PaddingValues,
    alSeleccionarDia: (Int) -> Unit,
    alVerEjercicio: (Long) -> Unit
) {
    val dia = rutina.dias.firstOrNull { it.numero == diaSeleccionado } ?: rutina.dias.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(relleno),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TarjetaSeccion {
                Text(rutina.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${rutina.objetivo?.nombre.orEmpty()} - ${Formatos.diasYMinutos(rutina.diasPorSemana, rutina.minutosPorSesion)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Generada el ${Formatos.fecha(rutina.generadaEn)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rutina.dias.forEach { d ->
                    FilterChip(
                        selected = d.numero == dia?.numero,
                        onClick = { alSeleccionarDia(d.numero) },
                        label = { Text("Día ${d.numero}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        if (dia != null) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(dia.enfoque, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${dia.minutosEstimados} min", color = MaterialTheme.colorScheme.primary)
                }
            }

            items(dia.ejercicios, key = { it.id }) { item ->
                TarjetaEjercicio(
                    posicion = dia.ejercicios.indexOf(item) + 1,
                    item = item,
                    alPulsar = { alVerEjercicio(item.id) }
                )
            }
        }

        item { AvisoSalud(Modifier.padding(top = 8.dp)) }
    }
}

@Composable
private fun TarjetaEjercicio(posicion: Int, item: ItemRutinaDto, alPulsar: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alPulsar),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("$posicion", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    Formatos.legible(item.grupoMuscular),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${Formatos.prescripcion(item.series, item.repeticiones, item.duracionSeg)} - ${Formatos.descanso(item.descansoSeg)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ver detalle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
