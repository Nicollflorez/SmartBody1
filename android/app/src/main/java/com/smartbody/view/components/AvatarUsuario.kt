package com.smartbody.view.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

/** Foto de perfil circular; si no hay foto muestra la inicial del nombre. */
@Composable
fun AvatarUsuario(foto: ByteArray?, nombre: String, tamano: Dp, modifier: Modifier = Modifier) {
    val imagen = remember(foto) {
        foto?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
    }

    if (imagen != null) {
        Image(
            bitmap = imagen,
            contentDescription = "Foto de perfil",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(tamano)
                .clip(CircleShape)
                .border(tamano / 36, MaterialTheme.colorScheme.outline, CircleShape)
        )
    } else {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = modifier.size(tamano)) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    nombre.firstOrNull()?.uppercase() ?: "S",
                    fontSize = (tamano.value * 0.42f).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
