package com.tcc.guardiaosms.classifier

import kotlinx.coroutines.delay

/**
 * Implementação PROVISÓRIA — não é o BERTimbau, é um placeholder para permitir
 * desenvolver e demonstrar o restante do app enquanto o modelo real não é
 * convertido para ONNX e integrado (ver [ClassifierRepository] e decisão 18
 * do log de arquitetura).
 *
 * Para que a demonstração do app não pareça aleatória enquanto o modelo real
 * não chega, a heurística usa alguns dos mesmos sinais superficiais que o
 * próprio TCC 1 identificou como vieses do dataset sintético (URLs encurtadas,
 * domínios suspeitos, palavras-gatilho como "urgente"/"bloqueio"/"Pix").
 * Isso é intencional: é o mesmo viés documentado no artigo, não uma tentativa
 * de simular o modelo real. Deve ser substituída pelo [OnnxClassifier]
 * assim que o modelo fine-tunado estiver disponível — NUNCA usar em produção.
 */
class MockClassifier : ClassifierRepository {

    private val suspiciousKeywords = listOf(
        "urgente", "bloqueio", "bloqueada", "bloqueado", "regularize", "regularizar",
        "pix", "cpf", "suspensa", "suspenso", "confirme", "valide", "protesto",
        "penhora", "expirando", "expira hoje"
    )
    private val suspiciousUrlHints = listOf(
        "bit.ly", "tiny.cc", "gg.gg", "t.me", ".mobi", ".info", "is.gd"
    )

    override suspend fun classify(messageText: String): ClassificationResult {
        // Pequeno atraso artificial só para simular o custo de uma inferência real
        // e permitir testar estados de "carregando" na UI.
        delay(120)

        val text = messageText.lowercase()
        var score = 0.05f
        suspiciousKeywords.forEach { if (text.contains(it)) score += 0.12f }
        suspiciousUrlHints.forEach { if (text.contains(it)) score += 0.2f }

        return ClassificationResult(scamProbability = score.coerceIn(0f, 1f))
    }

    override fun close() {
        // Nada a liberar nesta implementação provisória.
    }
}
