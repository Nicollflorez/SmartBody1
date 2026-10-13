package com.smartbody.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.smartbody.Contenedor
import com.smartbody.contenedor

/**
 * Crea un Presenter con las dependencias del contenedor de la aplicacion.
 * Se apoya en ViewModel unicamente para conservar el estado ante rotaciones.
 */
@Composable
inline fun <reified T : ViewModel> recordarPresenter(
    crossinline creador: (Contenedor) -> T
): T {
    val dependencias = LocalContext.current.contenedor
    return viewModel(
        factory = viewModelFactory {
            initializer { creador(dependencias) }
        }
    )
}
