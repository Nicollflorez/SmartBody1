package com.smartbody.view.routine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbody.presenter.routine.DetalleEjercicioPresenter
import com.smartbody.view.components.BarraSuperior
import com.smartbody.view.components.BotonSecundario
import com.smartbody.view.components.DatoResumen
import com.smartbody.view.components.Etiqueta
import com.smartbody.view.components.Formatos
import com.smartbody.view.components.PantallaCargando
import com.smartbody.view.components.PantallaError
import com.smartbody.view.components.TarjetaSeccion

/** RF08 - descripcion e instrucciones de un ejercicio. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetalleEjercicioScreen(
    presenter: DetalleEjercicioPresenter,
    alSustituir: () -> Unit,
    alVolver: () -> Unit
) {
    val estado by presenter.estado.collectAsStateWithLifecycle()

    Scaffold(topBar = { BarraSuperior("Ejercicio", alVolver = alVolver) }) { relleno ->
        val item = estado.item
        val ejercicio = estado.ejercicio
        when {
            estado.cargando -> PantallaCargando(Modifier.padding(relleno))
            item == null || ejercicio == null -> PantallaError(
                mensaje = estado.errorCarga ?: "No fue posible cargar el ejercicio.",
                alReintentar = presenter::cargar,
                modifier = Modifier.padding(relleno)
            )

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(relleno)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(ejercicio.nombre, style = MaterialTheme.typography.headlineSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Etiqueta(Formatos.legible(ejercicio.grupoMuscular))
                    Etiqueta(Formatos.legible(ejercicio.tipo))
                    ejercicio.equipamiento?.let { Etiqueta(if (it == "NINGUNO") "Sin equipo" else Formatos.legible(it)) }
                    ejercicio.nivelMinimo?.let { Etiqueta("Nivel ${Formatos.legible(it).lowercase()}") }
                }

                TarjetaSeccion(titulo = "Tu prescripción") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        DatoResumen("Series", "${item.series}", Modifier.weight(1f))
                        if (item.repeticiones != null) {
                            DatoResumen("Repeticiones", "${item.repeticiones}", Modifier.weight(1f))
                        } else if (item.duracionSeg != null) {
                            DatoResumen("Tiempo", Formatos.duracion(item.duracionSeg), Modifier.weight(1f))
                        }
                        DatoResumen("Descanso", if (item.descansoSeg > 0) Formatos.duracion(item.descansoSeg) else "-", Modifier.weight(1f))
                    }
                }

                ejercicio.descripcion?.let {
                    TarjetaSeccion(titulo = "Descripción") {
                        Text(it, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                if (estado.pasos.isNotEmpty()) {
                    TarjetaSeccion(titulo = "Cómo hacerlo") {
                        estado.pasos.forEachIndexed { indice, paso ->
                            Row(modifier = Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "${indice + 1}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Text("$paso.", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }

                BotonSecundario(texto = "Cambiar este ejercicio", onClick = alSustituir, icono = Icons.Filled.SwapHoriz)

                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Si sientes dolor durante el ejercicio, detente y consulta a un profesional de la salud.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
