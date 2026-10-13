package com.tcc.guardiaosms

import android.app.Application
import android.util.Log
import com.tcc.guardiaosms.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GuardiaoSmsApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        warmUpClassifier()
    }

    /**
     * Aquece o classificador em segundo plano assim que o processo inicia, para
     * que a cópia do modelo (~105 MB) e a criação da sessão ONNX não aconteçam
     * na hora em que o primeiro SMS chega. Se um SMS chegar durante o
     * aquecimento, ele espera a mesma inicialização (o `lazy` da sessão é
     * sincronizado) em vez de repeti-la.
     */
    private fun warmUpClassifier() {
        applicationScope.launch {
            try {
                container.classifierRepository.classify("Aquecimento do modelo")
            } catch (e: Exception) {
                Log.w(TAG, "Falha no aquecimento do classificador", e)
            }
        }
    }

    private companion object {
        const val TAG = "GuardiaoSmsApplication"
    }
}
