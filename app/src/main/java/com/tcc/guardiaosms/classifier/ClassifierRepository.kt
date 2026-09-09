package com.tcc.guardiaosms.classifier

/**
 * Resultado de uma classificação de mensagem.
 *
 * @param scamProbability probabilidade (0.0 a 1.0) de a mensagem ser golpe,
 *   equivalente à saída softmax da classe 1 ("golpe") do BERTimbau fine-tunado.
 */
data class ClassificationResult(
    val scamProbability: Float
) {
    fun isScam(threshold: Float): Boolean = scamProbability >= threshold
}

/**
 * Abstração da camada de classificação de mensagens.
 *
 * Existe uma interface aqui, em vez de o app chamar o ONNX Runtime diretamente,
 * por uma razão concreta e não apenas estética: o modelo fine-tunado (pesos
 * Hugging Face) ainda não foi convertido para ONNX nem disponibilizado a este
 * ambiente de desenvolvimento no momento da criação do app (ver decisão 18 do
 * log de arquitetura). Construir toda a UI, o histórico e as notificações
 * contra esta interface — e não contra uma classe concreta — permite:
 *
 *  1. Desenvolver e testar o app inteiro agora, com [MockClassifier];
 *  2. Trocar para [OnnxClassifier] depois sem alterar nenhuma outra camada;
 *  3. Testar a UI de forma determinística, sem depender do modelo pesado.
 */
interface ClassifierRepository {
    /** Deve ser chamado fora da thread principal (inferência pode levar dezenas de ms). */
    suspend fun classify(messageText: String): ClassificationResult

    /** Libera recursos nativos (sessão do ONNX Runtime), quando aplicável. */
    fun close()
}
