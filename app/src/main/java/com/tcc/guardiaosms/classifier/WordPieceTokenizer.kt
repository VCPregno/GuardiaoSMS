package com.tcc.guardiaosms.classifier

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Port manual do tokenizador WordPiece usado pelo BERTimbau
 * (neuralmind/bert-base-portuguese-cased).
 *
 * Por que um port manual em vez de uma lib pronta: não existe uma biblioteca
 * Android madura e mantida que replique fielmente o WordPiece de um
 * `vocab.txt` arbitrário do Hugging Face; a forma mais confiável de garantir
 * que o texto seja tokenizado EXATAMENTE como foi durante o treinamento
 * (mesmo vocab, mesma regra de "cased") é reimplementar o algoritmo
 * (ver decisão 20 do log de arquitetura).
 *
 * IMPORTANTE — pendência: este tokenizador precisa do arquivo `vocab.txt`
 * real gerado junto com o fine-tuning (o mesmo usado por
 * `AutoTokenizer.from_pretrained(...)` no treinamento). Ele deve ser colocado
 * em `app/src/main/assets/vocab.txt`. Sem esse arquivo, [OnnxClassifier]
 * não consegue ser inicializado corretamente.
 *
 * `doLowerCase = false` porque o BERTimbau usado é a variante "cased"
 * (ver `processar_bert.py`: `MODEL_NAME = "neuralmind/bert-base-portuguese-cased"`,
 * comentário `do_lower_case=False`).
 */
class WordPieceTokenizer(
    context: Context,
    assetFileName: String = "vocab.txt",
    private val doLowerCase: Boolean = false,
    private val maxLength: Int = 128
) {
    private val vocab: Map<String, Int>
    private val clsId: Int
    private val sepId: Int
    private val padId: Int
    private val unkId: Int
    private val unkToken = "[UNK]"

    init {
        val tokens = LinkedHashMap<String, Int>()
        context.assets.open(assetFileName).use { stream ->
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).forEachLine { line ->
                val token = line.trim()
                if (token.isNotEmpty()) tokens[token] = tokens.size
            }
        }
        vocab = tokens
        clsId = vocab["[CLS]"] ?: error("Token [CLS] ausente no vocab.txt")
        sepId = vocab["[SEP]"] ?: error("Token [SEP] ausente no vocab.txt")
        padId = vocab["[PAD]"] ?: error("Token [PAD] ausente no vocab.txt")
        unkId = vocab[unkToken] ?: error("Token [UNK] ausente no vocab.txt")
    }

    data class Encoding(
        val inputIds: LongArray,
        val attentionMask: LongArray,
        val tokenTypeIds: LongArray
    )

    fun encode(text: String): Encoding {
        val basicTokens = basicTokenize(text)
        val wordPieceTokens = mutableListOf<String>()
        basicTokens.forEach { wordPieceTokens.addAll(wordPiece(it)) }

        // Reserva 2 posições para [CLS] e [SEP]
        val truncated = wordPieceTokens.take(maxLength - 2)

        val ids = mutableListOf(clsId)
        truncated.forEach { ids.add(vocab[it] ?: unkId) }
        ids.add(sepId)

        val attention = MutableList(ids.size) { 1L }

        while (ids.size < maxLength) {
            ids.add(padId)
            attention.add(0L)
        }

        val inputIds = ids.map { it.toLong() }.toLongArray()
        val attentionMask = attention.toLongArray()
        val tokenTypeIds = LongArray(maxLength) { 0L }

        return Encoding(inputIds, attentionMask, tokenTypeIds)
    }

    /** Separação básica: espaços, pontuação e (opcionalmente) minúsculas. */
    private fun basicTokenize(text: String): List<String> {
        val normalized = if (doLowerCase) text.lowercase() else text
        val result = mutableListOf<String>()
        val current = StringBuilder()

        fun flush() {
            if (current.isNotEmpty()) {
                result.add(current.toString())
                current.clear()
            }
        }

        for (ch in normalized) {
            when {
                ch.isWhitespace() -> flush()
                isPunctuation(ch) -> {
                    flush()
                    result.add(ch.toString())
                }
                else -> current.append(ch)
            }
        }
        flush()
        return result
    }

    private fun isPunctuation(ch: Char): Boolean {
        val code = ch.code
        return (code in 33..47) || (code in 58..64) || (code in 91..96) || (code in 123..126)
    }

    /** Algoritmo greedy longest-match-first, padrão do WordPiece. */
    private fun wordPiece(token: String): List<String> {
        if (token.length > 200) return listOf(unkToken)

        val output = mutableListOf<String>()
        var start = 0
        while (start < token.length) {
            var end = token.length
            var found: String? = null
            while (start < end) {
                var candidate = token.substring(start, end)
                if (start > 0) candidate = "##$candidate"
                if (vocab.containsKey(candidate)) {
                    found = candidate
                    break
                }
                end -= 1
            }
            if (found == null) return listOf(unkToken)
            output.add(found)
            start = end
        }
        return output
    }
}
