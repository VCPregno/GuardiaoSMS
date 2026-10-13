package com.tcc.guardiaosms.ui.test

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcc.guardiaosms.classifier.ClassifierRepository
import com.tcc.guardiaosms.data.preferences.UserPreferencesRepository
import com.tcc.guardiaosms.risk.RiskLevel
import com.tcc.guardiaosms.tips.AttentionPoint
import com.tcc.guardiaosms.tips.AttentionPointsDetector
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Resultado de uma análise manual feita na tela de teste.
 *
 * Guarda também o threshold usado no momento da análise, para deixar
 * explícito na tela POR QUE aquela probabilidade virou (ou não) um alerta —
 * isso é importante na defesa perante a banca, já que o threshold de 0,3
 * é uma decisão calibrada nos experimentos do TCC 1, não um valor arbitrário.
 */
data class ManualTestResult(
    val analyzedText: String,
    val scamProbability: Float,
    val isScam: Boolean,
    val thresholdUsed: Float,
    val riskLevel: RiskLevel,
    val attentionPoints: Set<AttentionPoint>
)

class TestViewModel(
    private val classifier: ClassifierRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _result = MutableStateFlow<ManualTestResult?>(null)
    val result: StateFlow<ManualTestResult?> = _result.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    /** true quando a última análise falhou (ex.: modelo ausente ou erro do ONNX Runtime). */
    private val _analysisFailed = MutableStateFlow(false)
    val analysisFailed: StateFlow<Boolean> = _analysisFailed.asStateFlow()

    fun onTextChanged(value: String) {
        _inputText.value = value
        // Limpa o resultado anterior assim que o texto muda, para não deixar
        // na tela um veredito que não corresponde mais ao texto exibido.
        _result.value = null
        _analysisFailed.value = false
    }

    fun onAnalyze() {
        val text = _inputText.value
        if (text.isBlank() || _isAnalyzing.value) return

        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisFailed.value = false
            try {
                val threshold = preferencesRepository.threshold.first()
                val classification = classifier.classify(text)
                _result.value = ManualTestResult(
                    analyzedText = text,
                    scamProbability = classification.scamProbability,
                    isScam = classification.isScam(threshold),
                    thresholdUsed = threshold,
                    riskLevel = RiskLevel.from(classification.scamProbability, threshold),
                    attentionPoints = AttentionPointsDetector.detect(text)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Sem este catch, a exceção derrubaria o app (ex.: model.onnx ausente).
                Log.e(TAG, "Falha ao analisar a mensagem na tela Testar", e)
                _result.value = null
                _analysisFailed.value = true
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun onClear() {
        _inputText.value = ""
        _result.value = null
        _analysisFailed.value = false
    }

    private companion object {
        const val TAG = "TestViewModel"
    }
}
