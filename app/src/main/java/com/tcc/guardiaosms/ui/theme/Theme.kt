package com.tcc.guardiaosms.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalRiskColors = staticCompositionLocalOf { LightRiskColors }

/** Cores de risco do tema atual (claro ou escuro). */
val MaterialTheme.riskColors: RiskColors
    @Composable
    @ReadOnlyComposable
    get() = LocalRiskColors.current

@Composable
fun GuardiaoSmsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalRiskColors provides if (darkTheme) DarkRiskColors else LightRiskColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = GuardiaoSmsTypography,
            content = content
        )
    }
}
