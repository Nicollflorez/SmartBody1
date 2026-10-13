package com.smartbody.view.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Ilustracion del alimento (emoji) sobre un fondo suave. */
@Composable
fun IconoAlimento(icono: String?, modifier: Modifier = Modifier, tamano: Dp = 48.dp) {
    Surface(
        shape = RoundedCornerShape(tamano / 4),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.size(tamano)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(icono ?: "🍽️", fontSize = (tamano.value * 0.55f).sp)
        }
    }
}
