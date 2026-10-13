package com.smartbody.view.chat

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbody.model.dto.MensajeChat
import com.smartbody.presenter.chat.ChatPresenter
import com.smartbody.view.components.BarraSuperior

/** Chatbot de Smartbody: dudas sobre la rutina, la nutricion y el uso de la aplicacion. */
@Composable
fun ChatScreen(presenter: ChatPresenter, alVolver: () -> Unit) {
    val estado by presenter.estado.collectAsStateWithLifecycle()
    val lista = rememberLazyListState()

    // Siempre muestra el ultimo mensaje (o el indicador de "escribiendo").
    LaunchedEffect(estado.mensajes.size, estado.enviando) {
        val total = estado.mensajes.size + if (estado.enviando) 1 else 0
        if (total > 0) lista.animateScrollToItem(total - 1)
    }

    Scaffold(topBar = { BarraSuperior("Asistente Smartbody", alVolver = alVolver) }) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .imePadding()
        ) {
            LazyColumn(
                state = lista,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(estado.mensajes, key = { it.id }) { mensaje -> Burbuja(mensaje) }
                if (estado.enviando) {
                    item(key = "escribiendo") {
                        Burbuja(MensajeChat(id = -1, texto = "Escribiendo…", deUsuario = false), atenuada = true)
                    }
                }
            }

            if (estado.sugerencias.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    estado.sugerencias.forEach { sugerencia ->
                        SuggestionChip(
                            onClick = { presenter.elegirSugerencia(sugerencia) },
                            label = { Text(sugerencia) },
                            enabled = !estado.enviando
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = estado.texto,
                    onValueChange = presenter::alCambiarTexto,
                    placeholder = { Text("Escribe tu pregunta…") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { presenter.enviar() })
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = presenter::enviar,
                    enabled = estado.puedeEnviar,
                    modifier = Modifier.size(52.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}

@Composable
private fun Burbuja(mensaje: MensajeChat, atenuada: Boolean = false) {
    val deUsuario = mensaje.deUsuario
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (deUsuario) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!deUsuario) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.SmartToy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
        }
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (deUsuario) 18.dp else 4.dp,
                bottomEnd = if (deUsuario) 4.dp else 18.dp
            ),
            color = if (deUsuario) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                mensaje.texto,
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    atenuada -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}
