package com.tcc.guardiaosms.risk

import com.tcc.guardiaosms.classifier.ClassificationResult

/**
 * Nível de risco mostrado ao usuário, em palavras, no lugar da probabilidade.
 *
 * A decisão de ALERTA não muda: [HIGH] é exatamente `probabilidade >= limiar`
 * (a mesma regra de [ClassificationResult.isScam]), e só ele gera notificação.
 * [ATTENTION] cobre a faixa logo abaixo do limiar (de metade do limiar até o
 * limiar), em que o modelo não teve certeza para alertar nem para considerar a
 * mensagem segura. Por ser relativa ao limiar, a faixa acompanha o ajuste de
 * sensibilidade sem criar um segundo número fixo (ver DECISOES_ARQUITETURA.md).
 */
enum class RiskLevel {
    HIGH,
    ATTENTION,
    LOW;

    companion object {
        fun from(probability: Float, threshold: Float): RiskLevel = when {
            ClassificationResult(probability).isScam(threshold) -> HIGH
            probability >= threshold / 2 -> ATTENTION
            else -> LOW
        }

        /**
         * Para mensagens já salvas: usa a decisão gravada ([isScam]) e o limiar
         * do momento em que o SMS chegou, para o nível bater com o alerta dado.
         */
        fun fromRecord(isScam: Boolean, probability: Float, thresholdUsed: Float): RiskLevel = when {
            isScam -> HIGH
            probability >= thresholdUsed / 2 -> ATTENTION
            else -> LOW
        }
    }
}
