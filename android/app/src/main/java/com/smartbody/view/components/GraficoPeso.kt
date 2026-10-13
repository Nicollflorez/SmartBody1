package com.smartbody.view.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** Grafico de linea sencillo con la evolucion del peso (RF10). */
@Composable
fun GraficoPeso(pesos: List<Double>, modifier: Modifier = Modifier) {
    if (pesos.size < 2) {
        Text(
            "Registra al menos dos medidas para ver tu evolución.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
        return
    }

    val colorLinea = MaterialTheme.colorScheme.primary
    val colorGuia = MaterialTheme.colorScheme.outline
    val minimo = pesos.min()
    val maximo = pesos.max()
    val rango = (maximo - minimo).takeIf { it > 0 } ?: 1.0

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "${Formatos.numero(maximo)} kg",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(vertical = 8.dp)
        ) {
            val margen = 8.dp.toPx()
            val ancho = size.width - margen * 2
            val alto = size.height - margen * 2
            val paso = ancho / (pesos.size - 1)
            val puntos = pesos.mapIndexed { indice, peso ->
                Offset(
                    x = margen + indice * paso,
                    y = margen + alto - ((peso - minimo) / rango * alto).toFloat()
                )
            }

            listOf(margen, margen + alto / 2, margen + alto).forEach { y ->
                drawLine(colorGuia, Offset(margen, y), Offset(margen + ancho, y), strokeWidth = 1.dp.toPx())
            }

            val trazo = Path().apply {
                moveTo(puntos.first().x, puntos.first().y)
                puntos.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(
                trazo,
                colorLinea,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            puntos.forEach { drawCircle(colorLinea, radius = 4.dp.toPx(), center = it) }
        }
        Text(
            "${Formatos.numero(minimo)} kg",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
