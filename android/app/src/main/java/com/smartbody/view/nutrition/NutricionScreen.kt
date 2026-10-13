package com.smartbody.view.nutrition

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbody.model.dto.HidratacionDto
import com.smartbody.model.dto.ItemPlanDto
import com.smartbody.model.dto.PlanNutricionalDto
import com.smartbody.model.dto.RecetaDto
import com.smartbody.presenter.nutrition.NutricionPresenter
import com.smartbody.view.components.AvisoSalud
import com.smartbody.view.components.BarraSuperior
import com.smartbody.view.components.DatoResumen
import com.smartbody.view.components.EstadoVacio
import com.smartbody.view.components.Formatos
import com.smartbody.view.components.IconoAlimento
import com.smartbody.view.components.PantallaCargando
import com.smartbody.view.components.PantallaError
import com.smartbody.view.components.TarjetaSeccion

/** Plan nutricional vigente agrupado por comidas. */
@Composable
fun NutricionScreen(
    presenter: NutricionPresenter,
    barraInferior: @Composable () -> Unit,
    alSustituir: (planId: Long, itemId: Long) -> Unit,
    alGenerarPlan: () -> Unit
) {
    val estado by presenter.estado.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { presenter.cargar() }

    LaunchedEffect(estado.mensajeError, estado.plan) {
        val mensaje = estado.mensajeError
        if (mensaje != null && estado.plan != null) {
            snackbarHostState.showSnackbar(mensaje)
            presenter.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior(titulo = "Nutrición", acciones = {
                if (estado.plan != null) {
                    IconButton(onClick = alGenerarPlan) {
                        Icon(Icons.Filled.Autorenew, contentDescription = "Generar un nuevo plan")
                    }
                }
            })
        },
        bottomBar = barraInferior,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->
        val plan = estado.plan
        when {
            estado.cargando -> PantallaCargando(Modifier.padding(relleno))
            estado.sinPlan -> EstadoVacio(
                icono = Icons.Filled.Restaurant,
                titulo = "Aún no tienes un plan nutricional",
                mensaje = "Genera tu plan y aquí verás qué comer en cada momento del día.",
                textoBoton = "Generar mi plan",
                alPulsar = alGenerarPlan,
                modifier = Modifier.padding(relleno)
            )

            plan == null -> PantallaError(
                mensaje = estado.mensajeError ?: "No fue posible cargar tu plan nutricional.",
                alReintentar = presenter::cargar,
                modifier = Modifier.padding(relleno)
            )

            else -> ContenidoPlan(plan, relleno) { itemId -> alSustituir(plan.id, itemId) }
        }
    }
}

@Composable
private fun ContenidoPlan(plan: PlanNutricionalDto, relleno: PaddingValues, alSustituir: (Long) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(relleno),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TarjetaSeccion {
                Text(
                    "Calorías del día",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${Formatos.entero(plan.caloriasTotales)} kcal",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                plan.caloriasObjetivo?.let {
                    Text(
                        "Meta estimada para ${plan.objetivo?.nombre?.lowercase().orEmpty()}: ${Formatos.entero(it)} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DatoResumen("Proteína", "${Formatos.numero(plan.proteinaG)} g", Modifier.weight(1f))
                    DatoResumen("Carbohidratos", "${Formatos.numero(plan.carbohidratosG)} g", Modifier.weight(1f))
                    DatoResumen("Grasas", "${Formatos.numero(plan.grasasG)} g", Modifier.weight(1f))
                }
            }
        }

        plan.hidratacion?.let { hidratacion ->
            item(key = "hidratacion") { TarjetaHidratacion(hidratacion) }
        }

        plan.comidas.forEach { comida ->
            item(key = "comida-${comida.momento}") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(comida.nombre, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                    Text("${Formatos.entero(comida.calorias)} kcal", color = MaterialTheme.colorScheme.primary)
                }
            }
            items(comida.alimentos, key = { it.id }) { alimento ->
                TarjetaAlimento(alimento) { alSustituir(alimento.id) }
            }
            comida.receta?.let { receta ->
                item(key = "receta-${comida.momento}") { TarjetaReceta(receta) }
            }
        }

        item { AvisoSalud(Modifier.padding(top = 8.dp)) }
    }
}

/** Agua recomendada al día y consejos sobre bebidas. Usa un azul suave asociado al agua. */
@Composable
private fun TarjetaHidratacion(hidratacion: HidratacionDto) {
    var expandida by rememberSaveable { mutableStateOf(false) }
    val colorAgua = MaterialTheme.colorScheme.tertiary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = colorAgua)
                Spacer(Modifier.width(8.dp))
                Text("Hidratación", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${Formatos.numero(hidratacion.litrosDiaEntrenamiento)} litros de agua",
                style = MaterialTheme.typography.headlineSmall,
                color = colorAgua
            )
            Text(
                "En días de entrenamiento, unos ${hidratacion.vasosDiaEntrenamiento} vasos. " +
                    "En días de descanso, ${Formatos.numero(hidratacion.litrosDiaDescanso)} litros.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (expandida) {
                Spacer(Modifier.height(12.dp))
                BloqueConsejos(titulo = "Recomendaciones", consejos = hidratacion.recomendaciones)
                Spacer(Modifier.height(8.dp))
                BloqueConsejos(titulo = "Evita", consejos = hidratacion.evitar)
            }
            TextButton(
                onClick = { expandida = !expandida },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.textButtonColors(contentColor = colorAgua)
            ) {
                Text(if (expandida) "Ocultar consejos" else "Ver qué y cuándo beber")
            }
        }
    }
}

@Composable
private fun BloqueConsejos(titulo: String, consejos: List<String>) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            consejos.forEach { Vineta(it) }
        }
    }
}

/** Receta de la comida: ingredientes con cantidades, pasos y bebida sugerida. */
@Composable
private fun TarjetaReceta(receta: RecetaDto) {
    var expandida by rememberSaveable(receta.titulo) { mutableStateOf(false) }
    val acento = MaterialTheme.colorScheme.secondary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = acento)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Receta", style = MaterialTheme.typography.labelMedium, color = acento)
                    Text(receta.titulo, style = MaterialTheme.typography.titleMedium)
                }
            }

            if (expandida) {
                Spacer(Modifier.height(12.dp))
                Text("Ingredientes", style = MaterialTheme.typography.titleSmall)
                receta.ingredientes.forEach { Vineta(it) }

                Spacer(Modifier.height(12.dp))
                Text("Preparación", style = MaterialTheme.typography.titleSmall)
                receta.pasos.forEachIndexed { indice, paso ->
                    Row(modifier = Modifier.padding(vertical = 3.dp)) {
                        Text(
                            "${indice + 1}.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(24.dp)
                        )
                        Text(paso, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                receta.bebida?.let { bebida ->
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.LocalDrink,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Bebida: $bebida", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            TextButton(
                onClick = { expandida = !expandida },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.textButtonColors(contentColor = acento)
            ) {
                Text(if (expandida) "Ocultar receta" else "Ver receta")
            }
        }
    }
}

@Composable
private fun Vineta(texto: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(16.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun TarjetaAlimento(alimento: ItemPlanDto, alCambiar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconoAlimento(alimento.icono, tamano = 52.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(alimento.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${Formatos.numero(alimento.cantidad)} ${alimento.unidad} - ${alimento.calorias} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "P ${Formatos.numero(alimento.proteinaG)} g - C ${Formatos.numero(alimento.carbohidratosG)} g - G ${Formatos.numero(alimento.grasasG)} g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                alimento.alternativa?.let {
                    Text(
                        "Alternativa: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(onClick = alCambiar) { Text("Cambiar") }
        }
    }
}
