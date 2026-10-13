package com.smartbody.view.nutrition

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbody.presenter.nutrition.SustituirAlimentoPresenter
import com.smartbody.view.components.BarraSuperior
import com.smartbody.view.components.EstadoVacio
import com.smartbody.view.components.Formatos
import com.smartbody.view.components.ListaSeleccion
import com.smartbody.view.components.OpcionSeleccion
import com.smartbody.view.components.PantallaCargando
import com.smartbody.view.components.PantallaError

/** RF09 - eleccion de un alimento alternativo compatible. */
@Composable
fun SustituirAlimentoScreen(
    presenter: SustituirAlimentoPresenter,
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
        topBar = { BarraSuperior("Cambiar alimento", alVolver = alVolver) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->
        when {
            estado.cargando -> PantallaCargando(Modifier.padding(relleno))
            estado.errorCarga != null -> PantallaError(estado.errorCarga.orEmpty(), presenter::cargar, Modifier.padding(relleno))
            estado.alternativas.isEmpty() -> EstadoVacio(
                icono = Icons.Filled.Restaurant,
                titulo = "No hay alternativas",
                mensaje = "No encontramos otros alimentos de este grupo compatibles con tus restricciones.",
                textoBoton = "Volver",
                alPulsar = alVolver,
                modifier = Modifier.padding(relleno)
            )

            else -> ListaSeleccion(
                encabezado = "Reemplazar ${estado.actual?.nombre.orEmpty()}",
                explicacion = "Alimentos del mismo grupo compatibles con tus restricciones. La cantidad se ajusta para mantener las calorías.",
                opciones = estado.alternativas.map { a ->
                    OpcionSeleccion(
                        id = a.id,
                        titulo = a.nombre,
                        subtitulo = "${Formatos.numero(a.caloriasPor100g)} kcal por 100 g",
                        detalle = a.descripcion,
                        icono = a.icono
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
