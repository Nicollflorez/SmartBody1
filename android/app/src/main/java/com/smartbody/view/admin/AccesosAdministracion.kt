package com.smartbody.view.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartbody.presenter.admin.PestanaAdmin
import com.smartbody.view.components.AccesoRapido

/**
 * Modulos adicionales del administrador. Se muestran en Inicio y en Perfil, ademas de la aplicacion normal.
 */
@Composable
fun AccesosAdministracion(alAbrir: (PestanaAdmin) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Administración",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        AccesoRapido(
            icono = Icons.Filled.Dashboard,
            titulo = "Resumen",
            subtitulo = "Estadísticas e historial de uso",
            alPulsar = { alAbrir(PestanaAdmin.RESUMEN) }
        )
        AccesoRapido(
            icono = Icons.Filled.Group,
            titulo = "Usuarios",
            subtitulo = "Rutinas, planes y estado de cada cuenta",
            alPulsar = { alAbrir(PestanaAdmin.USUARIOS) }
        )
        AccesoRapido(
            icono = Icons.Filled.Assessment,
            titulo = "Reportes",
            subtitulo = "Efectividad: usuarios que han mejorado",
            alPulsar = { alAbrir(PestanaAdmin.REPORTES) }
        )
    }
}
