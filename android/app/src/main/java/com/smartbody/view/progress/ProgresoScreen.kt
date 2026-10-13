package com.smartbody.view.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbody.model.dto.MedidaDto
import com.smartbody.presenter.progress.ProgresoPresenter
import com.smartbody.presenter.progress.ProgresoUiState
import com.smartbody.view.components.BarraSuperior
import com.smartbody.view.components.BotonPrincipal
import com.smartbody.view.components.CampoTexto
import com.smartbody.view.components.DatoResumen
import com.smartbody.view.components.Formatos
import com.smartbody.view.components.GraficoPeso
import com.smartbody.view.components.PantallaCargando
import com.smartbody.view.components.PantallaError
import com.smartbody.view.components.TarjetaSeccion

/** RF10 - registro de medidas, grafico de evolucion e historial. */
@Composable
fun ProgresoScreen(
    presenter: ProgresoPresenter,
    barraInferior: @Composable () -> Unit
) {
    val estado by presenter.estado.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { presenter.cargar() }

    LaunchedEffect(estado.mensajeError, estado.mensajeExito) {
        val mensaje = estado.mensajeExito ?: estado.mensajeError
        if (mensaje != null) {
            snackbarHostState.showSnackbar(mensaje)
            presenter.mensajesMostrados()
        }
    }

    Scaffold(
        topBar = { BarraSuperior("Progreso") },
        bottomBar = barraInferior,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->
        when {
            estado.cargando -> PantallaCargando(Modifier.padding(relleno))
            estado.progreso == null -> PantallaError(
                mensaje = estado.errorCarga ?: "No fue posible cargar tu progreso.",
                alReintentar = presenter::cargar,
                modifier = Modifier.padding(relleno)
            )

            else -> ContenidoProgreso(estado, presenter, relleno)
        }
    }
}

@Composable
private fun ContenidoProgreso(estado: ProgresoUiState, presenter: ProgresoPresenter, relleno: PaddingValues) {
    val progreso = estado.progreso ?: return
    val historial = progreso.medidas.reversed()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(relleno),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (progreso.pesoActual != null) {
            item {
                TarjetaSeccion(titulo = "Resumen") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        DatoResumen("Inicial", "${Formatos.numero(progreso.pesoInicial ?: 0.0)} kg", Modifier.weight(1f))
                        DatoResumen("Actual", "${Formatos.numero(progreso.pesoActual)} kg", Modifier.weight(1f))
                        DatoResumen("Cambio", cambioDePeso(progreso.cambioPesoKg), Modifier.weight(1f))
                    }
                    progreso.imcActual?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "IMC actual: ${Formatos.decimal(it)} - ${progreso.clasificacionImc.orEmpty()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            item {
                TarjetaSeccion(titulo = "Evolución del peso") {
                    GraficoPeso(progreso.medidas.map { it.pesoKg })
                }
            }
        }

        item { FormularioMedidas(estado, presenter) }

        if (historial.isNotEmpty()) {
            item { Text("Historial", style = MaterialTheme.typography.headlineSmall) }
            items(historial, key = { it.id }) { medida -> FilaHistorial(medida) }
        }
    }
}

@Composable
private fun FormularioMedidas(estado: ProgresoUiState, presenter: ProgresoPresenter) {
    TarjetaSeccion(titulo = "Registrar medidas") {
        Text(
            "Solo el peso es obligatorio. Registra tus medidas cada semana o cada mes para ver tu avance.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        CampoTexto(
            valor = estado.peso,
            alCambiar = presenter::alCambiarPeso,
            etiqueta = "Peso (kg)",
            error = estado.errorPeso,
            tipoTeclado = KeyboardType.Decimal,
            habilitado = !estado.guardando
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CampoTexto(
                valor = estado.cintura,
                alCambiar = presenter::alCambiarCintura,
                etiqueta = "Cintura (cm)",
                error = estado.errorCintura,
                tipoTeclado = KeyboardType.Decimal,
                habilitado = !estado.guardando,
                modifier = Modifier.weight(1f)
            )
            CampoTexto(
                valor = estado.cadera,
                alCambiar = presenter::alCambiarCadera,
                etiqueta = "Cadera (cm)",
                error = estado.errorCadera,
                tipoTeclado = KeyboardType.Decimal,
                habilitado = !estado.guardando,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CampoTexto(
                valor = estado.pecho,
                alCambiar = presenter::alCambiarPecho,
                etiqueta = "Pecho (cm)",
                error = estado.errorPecho,
                tipoTeclado = KeyboardType.Decimal,
                habilitado = !estado.guardando,
                modifier = Modifier.weight(1f)
            )
            CampoTexto(
                valor = estado.brazo,
                alCambiar = presenter::alCambiarBrazo,
                etiqueta = "Brazo (cm)",
                error = estado.errorBrazo,
                tipoTeclado = KeyboardType.Decimal,
                accionTeclado = ImeAction.Done,
                habilitado = !estado.guardando,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(16.dp))
        BotonPrincipal(texto = "Guardar medidas", onClick = presenter::registrar, cargando = estado.guardando)
    }
}

@Composable
private fun FilaHistorial(medida: MedidaDto) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(Formatos.fecha(medida.registradoEn), style = MaterialTheme.typography.bodyMedium)
                val extras = listOfNotNull(
                    medida.cinturaCm?.let { "Cintura ${Formatos.numero(it)}" },
                    medida.caderaCm?.let { "Cadera ${Formatos.numero(it)}" },
                    medida.pechoCm?.let { "Pecho ${Formatos.numero(it)}" },
                    medida.brazoCm?.let { "Brazo ${Formatos.numero(it)}" }
                )
                if (extras.isNotEmpty()) {
                    Text(
                        extras.joinToString(" - ") + " cm",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${Formatos.numero(medida.pesoKg)} kg", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                medida.imc?.let {
                    Text("IMC ${Formatos.decimal(it)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.outline)
    }
}

private fun cambioDePeso(cambio: Double?): String = when {
    cambio == null -> "-"
    cambio > 0 -> "+${Formatos.numero(cambio)} kg"
    else -> "${Formatos.numero(cambio)} kg"
}
