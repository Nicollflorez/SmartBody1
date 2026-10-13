package com.smartbody.view.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.smartbody.navigation.Rutas

private data class Pestana(val ruta: String, val etiqueta: String, val icono: ImageVector)

private val PESTANAS = listOf(
    Pestana(Rutas.INICIO, "Inicio", Icons.Filled.Home),
    Pestana(Rutas.RUTINA, "Rutina", Icons.Filled.FitnessCenter),
    Pestana(Rutas.NUTRICION, "Nutrición", Icons.Filled.Restaurant),
    Pestana(Rutas.PROGRESO, "Progreso", Icons.Filled.Insights),
    Pestana(Rutas.PERFIL, "Perfil", Icons.Filled.Person)
)

/** Navegacion principal entre las secciones de la aplicacion. */
@Composable
fun BarraInferior(rutaActual: String, alNavegar: (String) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        PESTANAS.forEach { pestana ->
            NavigationBarItem(
                selected = rutaActual == pestana.ruta,
                onClick = { if (rutaActual != pestana.ruta) alNavegar(pestana.ruta) },
                icon = { Icon(pestana.icono, contentDescription = pestana.etiqueta) },
                label = { Text(pestana.etiqueta) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
