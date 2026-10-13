package com.smartbody.view.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbody.presenter.register.RegisterPresenter
import com.smartbody.view.components.BotonPrincipal
import com.smartbody.view.components.CampoPassword
import com.smartbody.view.components.CampoTexto
import com.smartbody.view.components.EnlaceTexto

/** RF01 - Pantalla de registro de usuarios. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    presenter: RegisterPresenter,
    alRegistrarse: () -> Unit,
    alVolver: () -> Unit
) {
    val estado by presenter.estado.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(estado.registroCompletado) {
        if (estado.registroCompletado) alRegistrarse()
    }

    LaunchedEffect(estado.mensajeError) {
        estado.mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            presenter.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Empieza a entrenar con un plan hecho para ti",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            CampoTexto(
                valor = estado.nombre,
                alCambiar = presenter::alCambiarNombre,
                etiqueta = "Nombre",
                error = estado.errorNombre,
                habilitado = !estado.cargando
            )

            Spacer(Modifier.height(16.dp))

            CampoTexto(
                valor = estado.email,
                alCambiar = presenter::alCambiarEmail,
                etiqueta = "Correo electrónico",
                error = estado.errorEmail,
                tipoTeclado = KeyboardType.Email,
                habilitado = !estado.cargando
            )

            Spacer(Modifier.height(16.dp))

            CampoPassword(
                valor = estado.password,
                alCambiar = presenter::alCambiarPassword,
                etiqueta = "Contraseña",
                error = estado.errorPassword,
                habilitado = !estado.cargando
            )

            Spacer(Modifier.height(16.dp))

            CampoPassword(
                valor = estado.confirmacion,
                alCambiar = presenter::alCambiarConfirmacion,
                etiqueta = "Confirmar contraseña",
                error = estado.errorConfirmacion,
                accionTeclado = ImeAction.Done,
                habilitado = !estado.cargando
            )

            Spacer(Modifier.height(28.dp))

            BotonPrincipal(
                texto = "Crear cuenta",
                onClick = presenter::registrar,
                cargando = estado.cargando
            )

            Spacer(Modifier.height(8.dp))

            EnlaceTexto(
                textoPlano = "¿Ya tienes cuenta?",
                textoEnlace = "Iniciar sesión",
                onClick = alVolver
            )
        }
    }
}
