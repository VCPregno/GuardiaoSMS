package com.tcc.guardiaosms.classifier

import android.content.Context
import java.io.BufferedReader
import java.io.InputStream
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
 *
 * Equivalência com o `tokenizer.json` do Hugging Face (BertNormalizer com
 * clean_text=True, handle_chinese_chars=True, strip_accents=None,
 * lowercase=False + BertPreTokenizer + WordPiece com
 * max_input_chars_per_word=100) verificada pelo teste unitário
 * `WordPieceTokenizerTest`, contra IDs gerados pelo próprio HF.
 */
class WordPieceTokenizer(
    vocabStream: InputStream,
    private val doLowerCase: Boolean = false,
    private val maxLength: Int = 128
) {
    constructor(
        context: Context,
        assetFileName: String = "vocab.txt",
        doLowerCase: Boolean = false,
        maxLength: Int = 128
    ) : this(context.assets.open(assetFileName), doLowerCase, maxLength)

    private val vocab: Map<String, Int>
    private val clsId: Int
    private val sepId: Int
    private val padId: Int
    private val unkId: Int
    private val unkToken = "[UNK]"

    init {
        val tokens = LinkedHashMap<String, Int>()
        vocabStream.use { stream ->
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

    /**
     * Normalização + pré-tokenização no estilo BertNormalizer/BertPreTokenizer,
     * percorrendo o texto por code point (emojis e outros caracteres fora do
     * BMP ocupam 2 Chars em UTF-16):
     * - remove NUL, U+FFFD e caracteres de controle/formatação (categorias C*,
     *   exceto \t \n \r), como U+200C e U+2068;
     * - espaços em branco Unicode separam palavras;
     * - toda pontuação (ASCII e Unicode) e ideogramas CJK viram tokens isolados.
     */
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

        var i = 0
        while (i < normalized.length) {
            val cp = normalized.codePointAt(i)
            i += Character.charCount(cp)
            when {
                cp == 0 || cp == 0xFFFD || isControl(cp) -> Unit
                isWhitespace(cp) -> flush()
                isPunctuation(cp) || isChineseChar(cp) -> {
                    flush()
                    result.add(String(Character.toChars(cp)))
                }
                else -> current.appendCodePoint(cp)
            }
        }
        flush()
        return result
    }

    /** \t, \n, \r e a propriedade Unicode White_Space (Zs, Zl, Zp; os demais Cc já foram removidos). */
    private fun isWhitespace(cp: Int): Boolean =
        cp == '\t'.code || cp == '\n'.code || cp == '\r'.code || Character.isSpaceChar(cp)

    /** Categorias C* (Cc, Cf, Cs, Co, Cn), exceto \t \n \r, que são tratados como espaço. */
    private fun isControl(cp: Int): Boolean {
        if (cp == '\t'.code || cp == '\n'.code || cp == '\r'.code) return false
        return when (Character.getType(cp)) {
            Character.CONTROL.toInt(),
            Character.FORMAT.toInt(),
            Character.SURROGATE.toInt(),
            Character.PRIVATE_USE.toInt(),
            Character.UNASSIGNED.toInt() -> true
            else -> false
        }
    }

    /** Pontuação ASCII (inclui símbolos como $ + < = > ^ ` | ~) ou pontuação Unicode (Pc, Pd, Ps, Pe, Pi, Pf, Po). */
    private fun isPunctuation(cp: Int): Boolean {
        if ((cp in 33..47) || (cp in 58..64) || (cp in 91..96) || (cp in 123..126)) return true
        return when (Character.getType(cp)) {
            Character.CONNECTOR_PUNCTUATION.toInt(),
            Character.DASH_PUNCTUATION.toInt(),
            Character.START_PUNCTUATION.toInt(),
            Character.END_PUNCTUATION.toInt(),
            Character.INITIAL_QUOTE_PUNCTUATION.toInt(),
            Character.FINAL_QUOTE_PUNCTUATION.toInt(),
            Character.OTHER_PUNCTUATION.toInt() -> true
            else -> false
        }
    }

    /** Mesmos blocos CJK considerados pelo BertNormalizer (handle_chinese_chars). */
    private fun isChineseChar(cp: Int): Boolean =
        cp in 0x4E00..0x9FFF ||
            cp in 0x3400..0x4DBF ||
            cp in 0x20000..0x2A6DF ||
            cp in 0x2A700..0x2B73F ||
            cp in 0x2B740..0x2B81F ||
            cp in 0x2B920..0x2CEAF ||
            cp in 0xF900..0xFAFF ||
            cp in 0x2F800..0x2FA1F

    /** Algoritmo greedy longest-match-first, padrão do WordPiece (limites sempre em code points). */
    private fun wordPiece(token: String): List<String> {
        if (token.codePointCount(0, token.length) > MAX_INPUT_CHARS_PER_WORD) return listOf(unkToken)

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
                end = token.offsetByCodePoints(end, -1)
            }
            if (found == null) return listOf(unkToken)
            output.add(found)
            start = end
        }
        return output
    }

    private companion object {
        /** Igual ao `max_input_chars_per_word` do tokenizer.json do modelo. */
        const val MAX_INPUT_CHARS_PER_WORD = 100
    }
}
