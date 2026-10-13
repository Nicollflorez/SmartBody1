package com.smartbody.view.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbody.presenter.plan.EtapaGeneracion
import com.smartbody.presenter.plan.GenerarPlanPresenter
import com.smartbody.view.components.AvisoSalud
import com.smartbody.view.components.BarraSuperior
import com.smartbody.view.components.BotonPrincipal
import com.smartbody.view.components.EstadoVacio
import com.smartbody.view.components.FilaDato
import com.smartbody.view.components.Formatos
import com.smartbody.view.components.PantallaCargando
import com.smartbody.view.components.PantallaError
import com.smartbody.view.components.TarjetaSeccion

/** RF06 - resumen de los datos considerados y generacion del plan. */
@Composable
fun GenerarPlanScreen(
    presenter: GenerarPlanPresenter,
    alGenerar: () -> Unit,
    alCompletarPerfil: () -> Unit,
    alVolver: () -> Unit
) {
    val estado by presenter.estado.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(estado.generado) {
        if (estado.generado) alGenerar()
    }

    LaunchedEffect(estado.mensajeError) {
        estado.mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            presenter.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = { BarraSuperior("Generar plan", alVolver = alVolver) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->
        val perfil = estado.perfil
        when {
            estado.cargando -> PantallaCargando(Modifier.padding(relleno))
            estado.errorCarga != null -> PantallaError(estado.errorCarga.orEmpty(), presenter::cargar, Modifier.padding(relleno))
            perfil == null || !perfil.completo -> EstadoVacio(
                icono = Icons.Filled.PersonAdd,
                titulo = "Primero completa tu perfil",
                mensaje = "Necesitamos tus datos físicos, objetivo y disponibilidad para generar tu plan.",
                textoBoton = "Completar perfil",
                alPulsar = alCompletarPerfil,
                modifier = Modifier.padding(relleno)
            )

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(relleno)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
                Text(
                    "Smartbody creará una rutina de entrenamiento y un plan nutricional a tu medida.",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                TarjetaSeccion(titulo = "Se tendrá en cuenta") {
                    FilaDato("Objetivo", perfil.objetivo?.nombre.orEmpty())
                    FilaDato("Nivel", Formatos.legible(perfil.nivelExperiencia))
                    FilaDato("Disponibilidad", Formatos.diasYMinutos(perfil.diasPorSemana, perfil.minutosPorSesion))
                    FilaDato(
                        "Datos físicos",
                        "${perfil.edad} años - ${perfil.pesoKg?.let(Formatos::numero)} kg - ${perfil.alturaCm?.let(Formatos::numero)} cm"
                    )
                    FilaDato(
                        "Restricciones",
                        perfil.restricciones.orEmpty().joinToString { it.nombre }.ifBlank { "Ninguna" }
                    )
                }

                Text(
                    "Si ya tienes un plan, será reemplazado por uno nuevo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                BotonPrincipal(
                    texto = "Generar mi plan",
                    onClick = presenter::generar,
                    cargando = estado.generando,
                    modifier = Modifier.fillMaxWidth()
                )

                estado.etapa?.let { etapa ->
                    Text(
                        when (etapa) {
                            EtapaGeneracion.RUTINA -> "Creando tu rutina de entrenamiento..."
                            EtapaGeneracion.NUTRICION -> "Armando tu plan nutricional..."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                AvisoSalud()
            }
        }
    }
}
