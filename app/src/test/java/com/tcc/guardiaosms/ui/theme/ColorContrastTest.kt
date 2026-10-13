package com.tcc.guardiaosms.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Verifica o contraste (WCAG 2.1) das paletas clara e escura:
 * texto >= 4,5:1 e elementos gráficos (ícones, barras, bordas) >= 3:1.
 */
class ColorContrastTest {

    private fun channel(c: Float): Double {
        val v = c.toDouble()
        return if (v <= 0.04045) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }

    private fun luminance(c: Color): Double =
        0.2126 * channel(c.red) + 0.7152 * channel(c.green) + 0.0722 * channel(c.blue)

    private fun contrast(a: Color, b: Color): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
    }

    private fun check(nome: String, fg: Color, bg: Color, minimo: Double, falhas: MutableList<String>) {
        val c = contrast(fg, bg)
        if (c < minimo) falhas += "$nome: %.2f:1 (mínimo %.1f:1)".format(c, minimo)
    }

    private fun checkScheme(tema: String, s: ColorScheme, r: RiskColors): List<String> {
        val f = mutableListOf<String>()
        val texto = 4.5
        val grafico = 3.0
        check("$tema onPrimary/primary", s.onPrimary, s.primary, texto, f)
        check("$tema onPrimaryContainer/primaryContainer", s.onPrimaryContainer, s.primaryContainer, texto, f)
        check("$tema onSecondary/secondary", s.onSecondary, s.secondary, texto, f)
        check("$tema onSecondaryContainer/secondaryContainer", s.onSecondaryContainer, s.secondaryContainer, texto, f)
        check("$tema onTertiaryContainer/tertiaryContainer", s.onTertiaryContainer, s.tertiaryContainer, texto, f)
        check("$tema onBackground/background", s.onBackground, s.background, texto, f)
        check("$tema onSurfaceVariant/surfaceVariant", s.onSurfaceVariant, s.surfaceVariant, texto, f)
        check("$tema onError/error", s.onError, s.error, texto, f)
        check("$tema onErrorContainer/errorContainer", s.onErrorContainer, s.errorContainer, texto, f)
        check("$tema inverseOnSurface/inverseSurface", s.inverseOnSurface, s.inverseSurface, texto, f)
        listOf(
            "surface" to s.surface,
            "surfaceContainerLowest" to s.surfaceContainerLowest,
            "surfaceContainerLow" to s.surfaceContainerLow,
            "surfaceContainer" to s.surfaceContainer,
            "surfaceContainerHigh" to s.surfaceContainerHigh,
            "surfaceContainerHighest" to s.surfaceContainerHighest
        ).forEach { (nome, bg) ->
            check("$tema onSurface/$nome", s.onSurface, bg, texto, f)
            check("$tema onSurfaceVariant/$nome", s.onSurfaceVariant, bg, texto, f)
            check("$tema primary/$nome (texto de botão)", s.primary, bg, texto, f)
            check("$tema error/$nome", s.error, bg, texto, f)
            check("$tema outline/$nome", s.outline, bg, grafico, f)
        }
        listOf("alto" to r.high, "atenção" to r.attention, "seguro" to r.safe).forEach { (nome, c) ->
            check("$tema risco $nome: texto/fundo", c.onContainer, c.container, texto, f)
            check("$tema risco $nome: ícone/fundo", c.accent, c.container, grafico, f)
            check("$tema risco $nome: barra/trilho", c.accent, s.surfaceVariant, grafico, f)
            check("$tema risco $nome: ícone/superfície", c.accent, s.surface, grafico, f)
        }
        return f
    }

    @Test
    fun temaClaroTemContrasteSuficiente() {
        val falhas = checkScheme("claro", LightColorScheme, LightRiskColors)
        assertTrue(falhas.joinToString("\n"), falhas.isEmpty())
    }

    @Test
    fun temaEscuroTemContrasteSuficiente() {
        val falhas = checkScheme("escuro", DarkColorScheme, DarkRiskColors)
        assertTrue(falhas.joinToString("\n"), falhas.isEmpty())
    }
}
