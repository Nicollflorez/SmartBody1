package com.smartbody.view.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Esquema unico oscuro, coherente con el logo (fondo negro, acentos verde neon).
 * Se usa siempre el mismo para mantener la identidad de marca en cualquier dispositivo.
 */
private val EsquemaSmartbody = darkColorScheme(
    primary = VerdeNeon,
    onPrimary = NegroLogo,
    primaryContainer = VerdeNoche,
    onPrimaryContainer = VerdeSuave,
    secondary = VerdeSuave,
    onSecondary = NegroLogo,
    secondaryContainer = CarbonClaro,
    onSecondaryContainer = BlancoHumo,
    tertiary = AzulAgua,
    onTertiary = NegroLogo,
    background = NegroLogo,
    onBackground = BlancoHumo,
    surface = Carbon,
    onSurface = BlancoHumo,
    surfaceVariant = CarbonClaro,
    onSurfaceVariant = GrisTexto,
    surfaceContainer = Carbon,
    surfaceContainerHigh = CarbonClaro,
    outline = BordeCarbon,
    outlineVariant = BordeCarbon,
    inversePrimary = VerdeProfundo,
    error = RojoError,
    onError = NegroLogo
)

@Composable
fun SmartbodyTheme(contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaSmartbody,
        typography = TipografiaSmartbody,
        content = contenido
    )
}
