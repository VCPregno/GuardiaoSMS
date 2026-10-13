package com.tcc.guardiaosms.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun porcentagemTruncadaComVirgula() {
        assertEquals("99,9%", formatProbabilityPercent(0.9997f))
        assertEquals("99,9%", formatProbabilityPercent(0.9999f))
        assertEquals("100,0%", formatProbabilityPercent(1f))
        assertEquals("0,0%", formatProbabilityPercent(0.0003f))
        assertEquals("0,0%", formatProbabilityPercent(0f))
        assertEquals("70,0%", formatProbabilityPercent(0.7f))
        assertEquals("30,0%", formatProbabilityPercent(0.3f))
        assertEquals("12,3%", formatProbabilityPercent(0.1239f))
        assertEquals("50,0%", formatProbabilityPercent(0.5f))
    }

    @Test
    fun limiarEDecimal() {
        assertEquals("30%", formatThresholdPercent(0.3f))
        assertEquals("5%", formatThresholdPercent(0.05f))
        assertEquals("0,30", formatDecimal(0.3f))
    }

    @Test
    fun dataHora() {
        val tz = java.util.TimeZone.getDefault()
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Sao_Paulo"))
            // 2026-10-13T19:37:00Z = 16:37 em São Paulo
            assertEquals("13/10/2026 às 16:37", formatDateTime(1791920220000L, "dd/MM/yyyy 'às' HH:mm"))
        } finally {
            java.util.TimeZone.setDefault(tz)
        }
    }
}
