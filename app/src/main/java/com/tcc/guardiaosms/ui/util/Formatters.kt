package com.tcc.guardiaosms.ui.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PT_BR = Locale("pt", "BR")

/**
 * Probabilidade (0..1) em porcentagem com 1 casa decimal e vírgula, TRUNCADA
 * em vez de arredondada: 0,9997 vira "99,9%" (e não "100,0%"), para nunca
 * sugerir uma certeza absoluta que o modelo não tem.
 *
 * Parte da representação decimal curta do Float ("0.7", e não 0.69999998...)
 * para o truncamento não perder um décimo por erro de ponto flutuante.
 */
fun formatProbabilityPercent(probability: Float): String {
    val percent = BigDecimal(probability.coerceIn(0f, 1f).toString())
        .movePointRight(2)
        .setScale(1, RoundingMode.DOWN)
    return String.format(PT_BR, "%.1f%%", percent)
}

/** Limiar (0..1) em porcentagem inteira, ex.: 0,3 -> "30%". */
fun formatThresholdPercent(threshold: Float): String =
    String.format(PT_BR, "%.0f%%", threshold * 100)

/** Número decimal com vírgula e 2 casas, ex.: 0,3 -> "0,30". */
fun formatDecimal(value: Float): String = String.format(PT_BR, "%.2f", value)

/** Data/hora em pt-BR com o padrão dado (o padrão vem de strings.xml). */
fun formatDateTime(epochMillis: Long, pattern: String): String =
    SimpleDateFormat(pattern, PT_BR).format(Date(epochMillis))
