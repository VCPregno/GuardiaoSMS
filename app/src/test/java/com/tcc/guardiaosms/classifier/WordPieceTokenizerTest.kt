package com.tcc.guardiaosms.classifier

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Compara os IDs do [WordPieceTokenizer] com os do tokenizer do Hugging Face
 * usado no treino. A referência (`tokenizer_reference.tsv`) é gerada pelo
 * script `scripts/gerar_referencia_tokenizer.py` do TCC-modelo, a partir de
 * todas as mensagens do dataset_v7.csv mais casos extras (pontuação Unicode,
 * caracteres invisíveis, emoji, CJK etc.).
 *
 * Formato de cada linha (separada por TAB):
 * nome, code points do texto, input_ids, attention_mask.
 */
class WordPieceTokenizerTest {

    private data class Caso(val nome: String, val texto: String, val ids: List<Long>, val mask: List<Long>)

    private val tokenizer = WordPieceTokenizer(File("src/main/assets/vocab.txt").inputStream())

    private val casos: List<Caso> by lazy {
        val stream = javaClass.classLoader!!.getResourceAsStream("tokenizer_reference.tsv")
            ?: error("tokenizer_reference.tsv não encontrado em src/test/resources")
        stream.bufferedReader(Charsets.UTF_8).readLines().filter { it.isNotEmpty() }.map { linha ->
            val (nome, codePoints, ids, mask) = linha.split('\t')
            val texto = buildString {
                codePoints.split(' ').filter { it.isNotEmpty() }.forEach { appendCodePoint(it.toInt()) }
            }
            Caso(nome, texto, ids.split(' ').map { it.toLong() }, mask.split(' ').map { it.toLong() })
        }
    }

    @Test
    fun idsIguaisAoTokenizerDoHuggingFace() {
        val divergentes = casos.filter { caso ->
            val enc = tokenizer.encode(caso.texto)
            enc.inputIds.toList() != caso.ids || enc.attentionMask.toList() != caso.mask ||
                enc.tokenTypeIds.any { it != 0L }
        }

        val total = casos.size
        val doDataset = casos.count { it.nome.startsWith("dataset_") }
        println("Tokenizer: ${total - divergentes.size}/$total mensagens iguais ao HF " +
            "($doDataset do dataset_v7.csv + ${total - doDataset} casos extras)")

        val detalhes = divergentes.take(10).joinToString("\n") { caso ->
            "${caso.nome} [${caso.texto}]\n  HF:     ${caso.ids.filter { it != 0L }}\n" +
                "  Kotlin: ${tokenizer.encode(caso.texto).inputIds.filter { it != 0L }}"
        }
        assertTrue("${divergentes.size} de $total mensagens divergem do HF:\n$detalhes", divergentes.isEmpty())
    }

    @Test
    fun referenciaContemDatasetECasosExtras() {
        assertEquals(3396, casos.count { it.nome.startsWith("dataset_") })
        listOf("reticencias", "aspas_curvas_travessao", "bullet_underscore", "invisiveis", "emoji").forEach { nome ->
            assertTrue("caso '$nome' ausente na referência", casos.any { it.nome == nome })
        }
    }
}
