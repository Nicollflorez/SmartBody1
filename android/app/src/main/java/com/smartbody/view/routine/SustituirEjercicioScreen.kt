package com.smartbody.view.routine

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbody.presenter.routine.SustituirEjercicioPresenter
import com.smartbody.view.components.BarraSuperior
import com.smartbody.view.components.EstadoVacio
import com.smartbody.view.components.Formatos
import com.smartbody.view.components.ListaSeleccion
import com.smartbody.view.components.OpcionSeleccion
import com.smartbody.view.components.PantallaCargando
import com.smartbody.view.components.PantallaError

/** RF09 - eleccion de un ejercicio alternativo compatible. */
@Composable
fun SustituirEjercicioScreen(
    presenter: SustituirEjercicioPresenter,
    alTerminar: () -> Unit,
    alVolver: () -> Unit
) {
    val estado by presenter.estado.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(estado.sustituido) {
        if (estado.sustituido) alTerminar()
    }

    LaunchedEffect(estado.mensajeError) {
        estado.mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            presenter.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = { BarraSuperior("Cambiar ejercicio", alVolver = alVolver) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->
        when {
            estado.cargando -> PantallaCargando(Modifier.padding(relleno))
            estado.errorCarga != null -> PantallaError(estado.errorCarga.orEmpty(), presenter::cargar, Modifier.padding(relleno))
            estado.alternativas.isEmpty() -> EstadoVacio(
                icono = Icons.Filled.SwapHoriz,
                titulo = "No hay alternativas",
                mensaje = "No encontramos otros ejercicios compatibles con tu nivel y tus restricciones.",
                textoBoton = "Volver",
                alPulsar = alVolver,
                modifier = Modifier.padding(relleno)
            )

            else -> ListaSeleccion(
                encabezado = "Reemplazar ${estado.actual?.nombre.orEmpty()}",
                explicacion = "Estas opciones son compatibles con tu nivel y tus restricciones. Se mantiene el tiempo del ejercicio.",
                opciones = estado.alternativas.map { e ->
                    OpcionSeleccion(
                        id = e.id,
                        titulo = e.nombre,
                        subtitulo = listOfNotNull(
                            Formatos.legible(e.grupoMuscular),
                            e.equipamiento?.let { if (it == "NINGUNO") "Sin equipo" else Formatos.legible(it) }
                        ).joinToString(" - "),
                        detalle = e.descripcion
                    )
                },
                seleccionado = estado.seleccionado,
                guardando = estado.guardando,
                textoConfirmar = "Confirmar cambio",
                alSeleccionar = presenter::seleccionar,
                alConfirmar = presenter::confirmar,
                modifier = Modifier.padding(relleno)
            )
        }
    }
}
