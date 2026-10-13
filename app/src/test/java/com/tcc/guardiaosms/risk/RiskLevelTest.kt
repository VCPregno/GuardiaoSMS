package com.tcc.guardiaosms.risk

import com.tcc.guardiaosms.classifier.ClassificationResult
import org.junit.Assert.assertEquals
import org.junit.Test

class RiskLevelTest {

    @Test
    fun limiarPadrao() {
        val limiar = 0.3f
        assertEquals(RiskLevel.HIGH, RiskLevel.from(0.9997f, limiar))
        assertEquals(RiskLevel.HIGH, RiskLevel.from(0.3f, limiar))        // p == limiar alerta
        assertEquals(RiskLevel.ATTENTION, RiskLevel.from(0.29f, limiar))
        assertEquals(RiskLevel.ATTENTION, RiskLevel.from(0.15f, limiar))   // p == limiar/2
        assertEquals(RiskLevel.LOW, RiskLevel.from(0.149f, limiar))
        assertEquals(RiskLevel.LOW, RiskLevel.from(0.0003f, limiar))
    }

    @Test
    fun faixaDeAtencaoAcompanhaOLimiar() {
        assertEquals(RiskLevel.ATTENTION, RiskLevel.from(0.5f, 0.8f))
        assertEquals(RiskLevel.LOW, RiskLevel.from(0.39f, 0.8f))
        assertEquals(RiskLevel.HIGH, RiskLevel.from(0.06f, 0.05f))
    }

    @Test
    fun riscoAltoEExatamenteADecisaoDeAlerta() {
        val limiares = listOf(0.05f, 0.3f, 0.5f, 0.95f)
        val probabilidades = (0..1000).map { it / 1000f }
        for (t in limiares) for (p in probabilidades) {
            assertEquals(
                "p=$p, limiar=$t",
                ClassificationResult(p).isScam(t),
                RiskLevel.from(p, t) == RiskLevel.HIGH
            )
        }
    }

    @Test
    fun registroSalvoUsaADecisaoGravada() {
        assertEquals(RiskLevel.HIGH, RiskLevel.fromRecord(isScam = true, probability = 0.31f, thresholdUsed = 0.3f))
        assertEquals(RiskLevel.ATTENTION, RiskLevel.fromRecord(isScam = false, probability = 0.2f, thresholdUsed = 0.3f))
        assertEquals(RiskLevel.LOW, RiskLevel.fromRecord(isScam = false, probability = 0.01f, thresholdUsed = 0.3f))
    }
}
