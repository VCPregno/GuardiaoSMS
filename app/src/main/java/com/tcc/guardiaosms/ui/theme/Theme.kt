package com.tcc.guardiaosms.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta alinhada com colors.xml — mantém consistência entre o tema do
// sistema (status bar) e o tema do Compose.
val BrandPrimary = Color(0xFF1B5E20)
val BrandPrimaryVariant = Color(0xFF2E7D32)
val AlertDanger = Color(0xFFC62828)
val AlertDangerBackground = Color(0xFFFDECEA)
val AlertSafe = Color(0xFF2E7D32)
val AlertSafeBackground = Color(0xFFE8F5E9)

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    secondary = BrandPrimaryVariant,
    error = AlertDanger
)

private val DarkColors = darkColorScheme(
    primary = BrandPrimaryVariant,
    secondary = BrandPrimary,
    error = AlertDanger
)

@Composable
fun GuardiaoSmsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = GuardiaoSmsTypography,
        content = content
    )
}
