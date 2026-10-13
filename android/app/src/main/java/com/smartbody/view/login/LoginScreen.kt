package com.smartbody.view.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbody.presenter.login.LoginPresenter
import com.smartbody.view.components.BotonPrincipal
import com.smartbody.view.components.CampoPassword
import com.smartbody.view.components.CampoTexto
import com.smartbody.view.components.EnlaceTexto
import com.smartbody.view.components.LogoSmartbody

/** RF02 - Pantalla de inicio de sesion. Solo muestra el estado que entrega el Presenter. */
@Composable
fun LoginScreen(
    presenter: LoginPresenter,
    alIniciarSesion: () -> Unit,
    alIrARegistro: () -> Unit
) {
    val estado by presenter.estado.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(estado.sesionIniciada) {
        if (estado.sesionIniciada) alIniciarSesion()
    }

    LaunchedEffect(estado.mensajeError) {
        estado.mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            presenter.mensajeMostrado()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LogoSmartbody()
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Bienvenido a Smartbody",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Inicia sesión para ver tu rutina",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

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
                accionTeclado = ImeAction.Done,
                habilitado = !estado.cargando
            )

            Spacer(Modifier.height(28.dp))

            BotonPrincipal(
                texto = "Iniciar sesión",
                onClick = presenter::iniciarSesion,
                cargando = estado.cargando
            )

            Spacer(Modifier.height(8.dp))

            EnlaceTexto(
                textoPlano = "¿No tienes cuenta?",
                textoEnlace = "Crear cuenta",
                onClick = alIrARegistro
            )
        }
    }
}
