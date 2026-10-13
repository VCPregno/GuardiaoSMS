package com.tcc.guardiaosms.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.tcc.guardiaosms.risk.RiskLevel

/*
 * Paletas clara e escura completas, na identidade verde (seguro) / vermelho (alerta).
 * Todos os pares texto/fundo têm contraste WCAG >= 4,5:1, e ícones/barras >= 3:1
 * (verificado por ColorContrastTest).
 */

internal val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1B5E20),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF0A3D0F),
    inversePrimary = Color(0xFF8FD88A),
    secondary = Color(0xFF2E7D32),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCEDC8),
    onSecondaryContainer = Color(0xFF1B3A12),
    tertiary = Color(0xFF00696E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFB2EBEE),
    onTertiaryContainer = Color(0xFF002022),
    background = Color(0xFFFBFDF7),
    onBackground = Color(0xFF1A1C19),
    surface = Color(0xFFFBFDF7),
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFDFE4D8),
    onSurfaceVariant = Color(0xFF3F473E),
    surfaceTint = Color(0xFF1B5E20),
    inverseSurface = Color(0xFF2F312D),
    inverseOnSurface = Color(0xFFF0F1EB),
    error = Color(0xFFB71C1C),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF6F786C),
    outlineVariant = Color(0xFFBFC9BB),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFBFDF7),
    surfaceDim = Color(0xFFDADCD5),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F7F1),
    surfaceContainer = Color(0xFFEFF2EA),
    surfaceContainerHigh = Color(0xFFE9ECE4),
    surfaceContainerHighest = Color(0xFFE3E6DE)
)

internal val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8FD88A),
    onPrimary = Color(0xFF003909),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFABF5A4),
    inversePrimary = Color(0xFF1B5E20),
    secondary = Color(0xFFA5D6A7),
    onSecondary = Color(0xFF0F3812),
    secondaryContainer = Color(0xFF2E4A2D),
    onSecondaryContainer = Color(0xFFC8E6C9),
    tertiary = Color(0xFF80D4DA),
    onTertiary = Color(0xFF00373A),
    tertiaryContainer = Color(0xFF004F53),
    onTertiaryContainer = Color(0xFF9CF0F6),
    background = Color(0xFF121411),
    onBackground = Color(0xFFE2E3DC),
    surface = Color(0xFF121411),
    onSurface = Color(0xFFE2E3DC),
    surfaceVariant = Color(0xFF424940),
    onSurfaceVariant = Color(0xFFC2C9BC),
    surfaceTint = Color(0xFF8FD88A),
    inverseSurface = Color(0xFFE2E3DC),
    inverseOnSurface = Color(0xFF2F312D),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF8C9387),
    outlineVariant = Color(0xFF424940),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF383A36),
    surfaceDim = Color(0xFF121411),
    surfaceContainerLowest = Color(0xFF0D0F0C),
    surfaceContainerLow = Color(0xFF1A1C19),
    surfaceContainer = Color(0xFF1E201D),
    surfaceContainerHigh = Color(0xFF282B27),
    surfaceContainerHighest = Color(0xFF333532)
)

/** Cores de um nível de risco: fundo do cartão, texto sobre ele e destaque (ícone/barra). */
@Immutable
data class RiskLevelColors(
    val container: Color,
    val onContainer: Color,
    val accent: Color
)

/** Cores semânticas de risco, com versões clara e escura (substituem as cores fixas antigas). */
@Immutable
data class RiskColors(
    val high: RiskLevelColors,
    val attention: RiskLevelColors,
    val safe: RiskLevelColors
) {
    fun forLevel(level: RiskLevel): RiskLevelColors = when (level) {
        RiskLevel.HIGH -> high
        RiskLevel.ATTENTION -> attention
        RiskLevel.LOW -> safe
    }
}

internal val LightRiskColors = RiskColors(
    high = RiskLevelColors(Color(0xFFFFDAD6), Color(0xFF690005), Color(0xFFC62828)),
    attention = RiskLevelColors(Color(0xFFFFE08A), Color(0xFF3D2E00), Color(0xFF7A5600)),
    safe = RiskLevelColors(Color(0xFFC8E6C9), Color(0xFF0A3D0F), Color(0xFF2E7D32))
)

internal val DarkRiskColors = RiskColors(
    high = RiskLevelColors(Color(0xFF8C1D18), Color(0xFFFFDAD6), Color(0xFFFFB4AB)),
    attention = RiskLevelColors(Color(0xFF5A4300), Color(0xFFFFE08A), Color(0xFFF5C542)),
    safe = RiskLevelColors(Color(0xFF1F4D22), Color(0xFFC8E6C9), Color(0xFF8FD88A))
)
