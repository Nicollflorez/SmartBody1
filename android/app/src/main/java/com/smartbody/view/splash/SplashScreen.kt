package com.smartbody.view.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbody.presenter.splash.DestinoInicial
import com.smartbody.presenter.splash.SplashPresenter
import com.smartbody.view.components.LogoSmartbody

/** Pantalla de inicio: decide si el usuario continua autenticado o debe iniciar sesion. */
@Composable
fun SplashScreen(
    presenter: SplashPresenter,
    alIrALogin: () -> Unit,
    alIrADashboard: () -> Unit
) {
    val destino by presenter.destino.collectAsStateWithLifecycle()

    LaunchedEffect(destino) {
        when (destino) {
            DestinoInicial.LOGIN -> alIrALogin()
            DestinoInicial.DASHBOARD -> alIrADashboard()
            null -> Unit
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LogoSmartbody(tamano = 200)
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Tu rutina, a tu medida",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))
            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
        }
    }
}
