package com.smartbody.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Opcion de una lista de reemplazos (ejercicios o alimentos). */
data class OpcionSeleccion(
    val id: Long,
    val titulo: String,
    val subtitulo: String,
    val detalle: String?,
    val icono: String? = null
)

/**
 * Pantalla comun de sustitucion (RF09): encabezado, lista de opciones con seleccion unica
 * y boton de confirmacion.
 */
@Composable
fun ListaSeleccion(
    encabezado: String,
    explicacion: String,
    opciones: List<OpcionSeleccion>,
    seleccionado: Long?,
    guardando: Boolean,
    textoConfirmar: String,
    alSeleccionar: (Long) -> Unit,
    alConfirmar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(encabezado, style = MaterialTheme.typography.headlineSmall)
                Text(explicacion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(opciones, key = { it.id }) { opcion ->
                val marcada = opcion.id == seleccionado
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { alSeleccionar(opcion.id) },
                    shape = RoundedCornerShape(16.dp),
                    border = if (marcada) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = marcada, onClick = { alSeleccionar(opcion.id) })
                        if (opcion.icono != null) {
                            IconoAlimento(opcion.icono, tamano = 44.dp)
                            Spacer(Modifier.width(12.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(opcion.titulo, style = MaterialTheme.typography.titleMedium)
                            Text(opcion.subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            opcion.detalle?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        BotonPrincipal(
            texto = textoConfirmar,
            onClick = alConfirmar,
            cargando = guardando,
            habilitado = seleccionado != null,
            modifier = Modifier.padding(20.dp)
        )
    }
}
