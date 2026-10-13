package com.tcc.guardiaosms.classifier

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.LongBuffer
import kotlin.math.exp

/**
 * Implementação REAL do classificador, usando o BERTimbau fine-tunado,
 * convertido para ONNX e quantizado em INT8.
 *
 * O modelo NÃO é lido para a memória com `readBytes()`: com ~104 MB, isso
 * estoura o heap da aplicação (OutOfMemoryError). Em vez disso, o arquivo é
 * copiado dos assets para o armazenamento interno na primeira execução (com
 * nome versionado, para que atualizações do modelo substituam a cópia antiga) e a
 * sessão é criada a partir do CAMINHO, deixando o ONNX Runtime mapear o
 * arquivo nativamente, fora do heap gerenciado.
 */
class OnnxClassifier(context: Context) : ClassifierRepository {

    private val appContext = context.applicationContext
    private val ortEnvironment = OrtEnvironment.getEnvironment()
    private val tokenizer = WordPieceTokenizer(appContext)

    private val session: OrtSession by lazy {
        val modelFile = File(appContext.filesDir, MODEL_FILE_NAME)

        // Remove cópias de versões anteriores do modelo (ex.: model.onnx, model_v1.onnx)
        // e temporários de cópias interrompidas (ex.: model_v2.onnx.tmp).
        appContext.filesDir.listFiles { file ->
            file.name != MODEL_FILE_NAME &&
                (file.name == MODEL_ASSET_NAME || OLD_MODEL_FILE_REGEX.matches(file.name))
        }?.forEach { it.delete() }

        if (!modelFile.exists() || modelFile.length() == 0L) {
            // Copia para um temporário e só renomeia no fim: se a cópia for
            // interrompida, não sobra um model_vN.onnx incompleto (que nunca
            // seria copiado de novo, pois o arquivo já existiria).
            val tempFile = File(appContext.filesDir, "$MODEL_FILE_NAME.tmp")
            appContext.assets.open(MODEL_ASSET_NAME).use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output, DEFAULT_BUFFER_SIZE)
                    output.fd.sync()
                }
            }
            if (!tempFile.renameTo(modelFile)) {
                tempFile.delete()
                throw IOException("Falha ao renomear ${tempFile.name} para ${modelFile.name}")
            }
        }

        ortEnvironment.createSession(modelFile.absolutePath, OrtSession.SessionOptions())
    }

    override suspend fun classify(messageText: String): ClassificationResult =
        withContext(Dispatchers.Default) {
            val encoding = tokenizer.encode(messageText)
            val shape = longArrayOf(1, encoding.inputIds.size.toLong())

            OnnxTensor.createTensor(ortEnvironment, LongBuffer.wrap(encoding.inputIds), shape).use { inputIdsTensor ->
                OnnxTensor.createTensor(ortEnvironment, LongBuffer.wrap(encoding.attentionMask), shape).use { attentionMaskTensor ->
                    OnnxTensor.createTensor(ortEnvironment, LongBuffer.wrap(encoding.tokenTypeIds), shape).use { tokenTypeTensor ->

                        val inputs = mapOf(
                            "input_ids" to inputIdsTensor,
                            "attention_mask" to attentionMaskTensor,
                            "token_type_ids" to tokenTypeTensor
                        )

                        session.run(inputs).use { results ->
                            @Suppress("UNCHECKED_CAST")
                            val logits = (results[0].value as Array<FloatArray>)[0]
                            ClassificationResult(scamProbability = softmaxScamProbability(logits))
                        }
                    }
                }
            }
        }

    /** Aplica softmax nos 2 logits (classe 0 = legítima, classe 1 = golpe) e retorna P(golpe). */
    private fun softmaxScamProbability(logits: FloatArray): Float {
        require(logits.size == 2) { "Esperado 2 logits (legítima/golpe), recebido ${logits.size}" }
        val maxLogit = maxOf(logits[0], logits[1])
        val expLegit = exp((logits[0] - maxLogit).toDouble())
        val expScam = exp((logits[1] - maxLogit).toDouble())
        return (expScam / (expLegit + expScam)).toFloat()
    }

    override fun close() {
        session.close()
    }

    companion object {
        private const val MODEL_ASSET_NAME = "model.onnx"

        /** Incrementar sempre que o model.onnx dos assets for trocado, para forçar nova cópia. */
        private const val MODEL_VERSION = 2
        private const val MODEL_FILE_NAME = "model_v$MODEL_VERSION.onnx"
        private val OLD_MODEL_FILE_REGEX = Regex("""model_v\d+\.onnx(\.tmp)?""")
    }
}