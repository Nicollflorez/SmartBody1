package com.smartbody.view.prediccion

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbody.model.dto.PrediccionDto
import com.smartbody.model.dto.SeguimientoPrediccionDto
import com.smartbody.presenter.prediccion.ConstructorPeticionPrediccion
import com.smartbody.presenter.prediccion.DatosBase
import com.smartbody.presenter.prediccion.PrediccionPresenter
import com.smartbody.presenter.prediccion.PrediccionUiState
import com.smartbody.view.components.AvisoSalud
import com.smartbody.view.components.BarraSuperior
import com.smartbody.view.components.BotonPrincipal
import com.smartbody.view.components.BotonSecundario
import com.smartbody.view.components.CampoTexto
import com.smartbody.view.components.DatoResumen
import com.smartbody.view.components.Formatos
import com.smartbody.view.components.GraficoPeso
import com.smartbody.view.components.PantallaCargando
import com.smartbody.view.components.PantallaError
import com.smartbody.view.components.TarjetaSeccion

/**
 * Tiempo estimado hasta el peso objetivo y progreso real frente a la estimacion.
 * Presenta el estado del presenter; no calcula nada aqui.
 */
@Composable
fun PrediccionScreen(
    presenter: PrediccionPresenter,
    alVolver: () -> Unit
) {
    val estado by presenter.estado.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { presenter.cargar() }

    LaunchedEffect(estado.mensajeError) {
        estado.mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            presenter.mensajesMostrados()
        }
    }

    Scaffold(
        topBar = { BarraSuperior("Tiempo hasta tu objetivo", alVolver = alVolver) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->
        val error = estado.errorCarga
        when {
            estado.cargando -> PantallaCargando(Modifier.padding(relleno))
            error != null -> PantallaError(
                mensaje = error,
                alReintentar = presenter::cargar,
                modifier = Modifier.padding(relleno)
            )

            estado.base != null -> ContenidoPrediccion(estado, presenter, Modifier.padding(relleno))
        }
    }
}

@Composable
private fun ContenidoPrediccion(
    estado: PrediccionUiState,
    presenter: PrediccionPresenter,
    modifier: Modifier
) {
    val base = estado.base ?: return
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AvisoOrientativo()
        TarjetaDatos(base, estado.rutinaActiva)

        if (estado.faltantes.isNotEmpty()) {
            TarjetaSeccion(titulo = "Completa estos datos") {
                estado.faltantes.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
            }
        }

        FormularioEstimacion(estado, presenter)

        estado.prediccion?.let { ResultadoEstimacion(it) }

        if (estado.cargandoSeguimiento) {
            Text("Cargando tu progreso real…", style = MaterialTheme.typography.bodyMedium)
        }
        estado.seguimiento?.let { seguimiento ->
            SeguimientoReal(seguimiento)
            BotonSecundario(
                texto = "Actualizar progreso real",
                onClick = presenter::actualizarSeguimiento
            )
        }

        AvisoSalud()
    }
}

@Composable
private fun AvisoOrientativo() {
    Text(
        "Es una estimación orientativa, no una garantía de pérdida ni de ganancia de peso.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun TarjetaDatos(base: DatosBase, rutinaActiva: Boolean) {
    TarjetaSeccion(titulo = "Tus datos") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            DatoResumen(
                etiqueta = "Peso actual",
                valor = base.pesoActualKg?.let { "${Formatos.numero(it)} kg" } ?: "Sin registrar",
                modifier = Modifier.weight(1f)
            )
            DatoResumen(
                etiqueta = "Objetivo",
                valor = Formatos.legible(base.objetivoCodigo),
                modifier = Modifier.weight(1.4f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Calorías objetivo del plan: ${base.caloriasObjetivo?.let { "${Formatos.entero(it)} kcal" } ?: "sin plan"}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Plan de entrenamiento activo: ${if (rutinaActiva) "sí" else "no"}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun FormularioEstimacion(estado: PrediccionUiState, presenter: PrediccionPresenter) {
    val entrada = estado.entrada
    TarjetaSeccion(titulo = "Calcular tiempo estimado") {
        CampoTexto(
            valor = entrada.pesoObjetivo,
            alCambiar = presenter::alCambiarPesoObjetivo,
            etiqueta = "Peso objetivo (kg)",
            error = estado.errores["pesoObjetivo"],
            tipoTeclado = KeyboardType.Decimal
        )
        Spacer(Modifier.height(12.dp))
        Text("Nivel de actividad", style = MaterialTheme.typography.labelLarge)
        FilaOpciones(
            opciones = ConstructorPeticionPrediccion.NIVELES_ACTIVIDAD,
            seleccionada = entrada.nivelActividad,
            alSeleccionar = presenter::alCambiarNivelActividad
        )
        Spacer(Modifier.height(8.dp))
        Text("Tipo de actividad", style = MaterialTheme.typography.labelLarge)
        FilaOpciones(
            opciones = ConstructorPeticionPrediccion.TIPOS_ACTIVIDAD,
            seleccionada = entrada.tipoActividad,
            alSeleccionar = presenter::alCambiarTipoActividad
        )
        Spacer(Modifier.height(12.dp))
        CampoTexto(
            valor = entrada.adherenciaNutricional,
            alCambiar = presenter::alCambiarAdherenciaNutricional,
            etiqueta = "Seguimiento del plan nutricional (%)",
            error = estado.errores["adherenciaNutricional"],
            tipoTeclado = KeyboardType.Decimal
        )
        CampoTexto(
            valor = entrada.adherenciaEjercicio,
            alCambiar = presenter::alCambiarAdherenciaEjercicio,
            etiqueta = "Seguimiento de las rutinas (%)",
            error = estado.errores["adherenciaEjercicio"],
            tipoTeclado = KeyboardType.Decimal
        )
        Text(
            "Calcula cuánto del plan y de las sesiones sigues, en tus primeras dos semanas.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        BotonPrincipal(
            texto = "Calcular tiempo estimado",
            onClick = presenter::calcular,
            cargando = estado.calculando
        )
    }
}

@Composable
private fun FilaOpciones(opciones: List<String>, seleccionada: String, alSeleccionar: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opciones.forEach { opcion ->
            FilterChip(
                selected = opcion == seleccionada,
                onClick = { alSeleccionar(opcion) },
                label = { Text(Formatos.legible(opcion)) }
            )
        }
    }
}

@Composable
private fun ResultadoEstimacion(prediccion: PrediccionDto) {
    TarjetaSeccion(titulo = "Tu estimación") {
        Text(
            "${Formatos.numero(prediccion.semanasEstimadas)} semanas",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Fecha aproximada: ${Formatos.fechaSola(prediccion.fechaAproximada)}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Calculada el ${Formatos.fecha(prediccion.fechaPrediccion)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            prediccion.aviso,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SeguimientoReal(seguimiento: SeguimientoPrediccionDto) {
    TarjetaSeccion(titulo = "Tu progreso real") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            DatoResumen(
                etiqueta = "Peso actual",
                valor = seguimiento.pesoActual?.let { "${Formatos.numero(it)} kg" } ?: "Sin medidas",
                modifier = Modifier.weight(1f)
            )
            DatoResumen(
                etiqueta = "Avance",
                valor = seguimiento.avancePorcentaje?.let { "${Formatos.decimal(it, 0)} %" } ?: "-",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            textoEstado(seguimiento),
            style = MaterialTheme.typography.bodyMedium
        )
        seguimiento.diferenciaSemanas?.let { diferencia ->
            val signo = if (diferencia > 0) "+" else ""
            Text(
                "Diferencia entre la estimación y lo real: $signo${Formatos.decimal(diferencia)} semanas",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        seguimiento.semanasRestantesEstimadas?.let {
            Text(
                "Semanas restantes según la estimación: ${Formatos.decimal(it)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        seguimiento.ritmoRealKgSemana?.let {
            Text(
                "Ritmo real: ${Formatos.decimal(it, 2)} kg por semana",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(Modifier.height(12.dp))
        GraficoPeso(pesos = seguimiento.puntos.map { it.pesoKg })
        seguimiento.ultimaActualizacion?.let {
            Text(
                "Última actualización: ${Formatos.fechaHora(it)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun textoEstado(seguimiento: SeguimientoPrediccionDto): String = when (seguimiento.estado) {
    "OBJETIVO_ALCANZADO" -> "¡Alcanzaste tu objetivo en ${Formatos.decimal(seguimiento.semanasRealesHastaObjetivo ?: 0.0)} semanas!"
    "EN_SEGUIMIENTO" -> "Seguimiento en curso: registra tu peso en Progreso para ver tu avance."
    else -> "Aún no tienes medidas posteriores a esta estimación. Regístralas en Progreso."
}
