package com.smartbody.view.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbody.model.dto.RestriccionDto
import com.smartbody.presenter.profile.PerfilFormPresenter
import com.smartbody.presenter.profile.PerfilFormUiState
import com.smartbody.view.components.AvisoSalud
import com.smartbody.view.components.BarraSuperior
import com.smartbody.view.components.BotonPrincipal
import com.smartbody.view.components.BotonSecundario
import com.smartbody.view.components.CampoTexto
import com.smartbody.view.components.Formatos
import com.smartbody.view.components.PantallaCargando
import com.smartbody.view.components.PantallaError

private val TITULOS_PASOS = listOf("Datos personales", "Tu objetivo", "Disponibilidad", "Restricciones")

/** RF03, RF04 y RF05 - asistente de perfil en cuatro pasos. */
@Composable
fun EditarPerfilScreen(
    presenter: PerfilFormPresenter,
    alGuardar: () -> Unit,
    alVolver: () -> Unit
) {
    val estado by presenter.estado.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) alGuardar()
    }

    LaunchedEffect(estado.mensajeError) {
        estado.mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            presenter.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Mi perfil",
                alVolver = { if (estado.paso > 0) presenter.anterior() else alVolver() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->
        when {
            estado.cargando -> PantallaCargando(Modifier.padding(relleno))
            estado.errorCarga != null -> PantallaError(
                mensaje = estado.errorCarga.orEmpty(),
                alReintentar = presenter::cargar,
                modifier = Modifier.padding(relleno)
            )

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(relleno)
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    LinearProgressIndicator(
                        progress = { (estado.paso + 1f) / PerfilFormPresenter.TOTAL_PASOS },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Paso ${estado.paso + 1} de ${PerfilFormPresenter.TOTAL_PASOS}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(TITULOS_PASOS[estado.paso], style = MaterialTheme.typography.headlineSmall)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    when (estado.paso) {
                        0 -> PasoDatosPersonales(estado, presenter)
                        1 -> PasoObjetivo(estado, presenter)
                        2 -> PasoDisponibilidad(estado, presenter)
                        else -> PasoRestricciones(estado, presenter)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (estado.paso > 0) {
                        BotonSecundario("Atrás", presenter::anterior, Modifier.weight(1f))
                    }
                    BotonPrincipal(
                        texto = if (estado.esUltimoPaso) "Guardar perfil" else "Siguiente",
                        onClick = presenter::siguiente,
                        cargando = estado.guardando,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PasoDatosPersonales(estado: PerfilFormUiState, presenter: PerfilFormPresenter) {
    Text(
        "Estos datos permiten ajustar la intensidad de tu rutina y las calorías de tu plan.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(20.dp))

    CampoTexto(
        valor = estado.edad,
        alCambiar = presenter::alCambiarEdad,
        etiqueta = "Edad (años)",
        error = estado.errorEdad,
        tipoTeclado = KeyboardType.Number
    )
    Spacer(Modifier.height(16.dp))

    Text("Sexo", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PerfilFormPresenter.SEXOS.forEach { sexo ->
            ChipOpcion(
                texto = Formatos.legible(sexo),
                seleccionado = estado.sexo == sexo,
                alPulsar = { presenter.alSeleccionarSexo(sexo) }
            )
        }
    }
    TextoError(estado.errorSexo)
    Spacer(Modifier.height(16.dp))

    CampoTexto(
        valor = estado.peso,
        alCambiar = presenter::alCambiarPeso,
        etiqueta = "Peso (kg)",
        error = estado.errorPeso,
        tipoTeclado = KeyboardType.Decimal
    )
    Spacer(Modifier.height(16.dp))
    CampoTexto(
        valor = estado.altura,
        alCambiar = presenter::alCambiarAltura,
        etiqueta = "Altura (cm)",
        error = estado.errorAltura,
        tipoTeclado = KeyboardType.Decimal,
        accionTeclado = ImeAction.Done
    )

    estado.imcEstimado?.let { imc ->
        Spacer(Modifier.height(16.dp))
        Text(
            "Tu IMC estimado es ${Formatos.decimal(imc)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PasoObjetivo(estado: PerfilFormUiState, presenter: PerfilFormPresenter) {
    Text(
        "Elige lo que quieres lograr. Tu rutina y tu plan nutricional se construyen a partir de este objetivo.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(16.dp))
    TextoError(estado.errorObjetivo)

    estado.objetivos.forEach { objetivo ->
        TarjetaSeleccionable(
            seleccionada = estado.objetivoId == objetivo.id,
            alPulsar = { presenter.alSeleccionarObjetivo(objetivo.id) }
        ) {
            RadioButton(selected = estado.objetivoId == objetivo.id, onClick = { presenter.alSeleccionarObjetivo(objetivo.id) })
            Spacer(Modifier.width(8.dp))
            Column {
                Text(objetivo.nombre, style = MaterialTheme.typography.titleMedium)
                objetivo.descripcion?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PasoDisponibilidad(estado: PerfilFormUiState, presenter: PerfilFormPresenter) {
    Text("Nivel de experiencia", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PerfilFormPresenter.NIVELES.forEach { nivel ->
            ChipOpcion(
                texto = Formatos.legible(nivel),
                seleccionado = estado.nivel == nivel,
                alPulsar = { presenter.alSeleccionarNivel(nivel) }
            )
        }
    }
    TextoError(estado.errorNivel)
    Text(
        "Principiante: menos de 6 meses entrenando. Intermedio: entre 6 meses y 2 años. Avanzado: más de 2 años.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(Modifier.height(24.dp))
    Text("Días disponibles por semana", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        (1..7).forEach { dia ->
            val seleccionado = estado.dias == dia
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { presenter.alCambiarDias(dia) },
                shape = CircleShape,
                color = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$dia",
                        fontWeight = FontWeight.SemiBold,
                        color = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(24.dp))
    Text("Tiempo por sesión", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PerfilFormPresenter.OPCIONES_MINUTOS.forEach { minutos ->
            ChipOpcion(
                texto = "$minutos min",
                seleccionado = estado.minutos == minutos,
                alPulsar = { presenter.alCambiarMinutos(minutos) }
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    Text(
        "Tu rutina se ajustará para durar aproximadamente ${estado.minutos} minutos.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun PasoRestricciones(estado: PerfilFormUiState, presenter: PerfilFormPresenter) {
    AvisoSalud()
    Spacer(Modifier.height(8.dp))
    Text(
        "Marca las que apliquen. Los ejercicios y alimentos incompatibles no se incluirán en tu plan. Si no tienes ninguna, continúa.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    GrupoRestricciones("Salud", estado.restricciones.filter { it.tipo == "SALUD" }, estado, presenter)
    GrupoRestricciones("Alimentación", estado.restricciones.filter { it.tipo == "ALIMENTACION" }, estado, presenter)
}

@Composable
private fun GrupoRestricciones(
    titulo: String,
    restricciones: List<RestriccionDto>,
    estado: PerfilFormUiState,
    presenter: PerfilFormPresenter
) {
    if (restricciones.isEmpty()) return
    Spacer(Modifier.height(20.dp))
    Text(titulo, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(4.dp))
    restricciones.forEach { restriccion ->
        val marcada = restriccion.id in estado.restriccionesSeleccionadas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { presenter.alAlternarRestriccion(restriccion.id) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = marcada, onCheckedChange = { presenter.alAlternarRestriccion(restriccion.id) })
            Column {
                Text(restriccion.nombre, style = MaterialTheme.typography.bodyLarge)
                restriccion.descripcion?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ChipOpcion(texto: String, seleccionado: Boolean, alPulsar: () -> Unit) {
    FilterChip(
        selected = seleccionado,
        onClick = alPulsar,
        label = { Text(texto) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun TarjetaSeleccionable(
    seleccionada: Boolean,
    alPulsar: () -> Unit,
    contenido: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alPulsar),
        shape = RoundedCornerShape(16.dp),
        border = if (seleccionada) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            contenido()
        }
    }
}

@Composable
private fun TextoError(error: String?) {
    if (error != null) {
        Text(
            error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )
    }
}
