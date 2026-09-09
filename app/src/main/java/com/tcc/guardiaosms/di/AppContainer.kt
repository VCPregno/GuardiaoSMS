package com.tcc.guardiaosms.di

import android.content.Context
import com.tcc.guardiaosms.classifier.ClassifierRepository
import com.tcc.guardiaosms.classifier.OnnxClassifier
import com.tcc.guardiaosms.data.local.AppDatabase
import com.tcc.guardiaosms.data.preferences.UserPreferencesRepository
import com.tcc.guardiaosms.notification.AlertNotifier

/**
 * Container manual de dependências (sem Hilt/Koin).
 *
 * Decisão consciente de simplicidade: para o escopo e o prazo de um TCC,
 * um framework de injeção de dependência adicionaria complexidade de
 * configuração sem benefício real no tamanho atual do projeto. Este único
 * arquivo concentra a montagem de dependências, o que também facilita
 * explicar a arquitetura para a banca.
 *
 * >>> PONTO ÚNICO DE TROCA mock → real <
 * Troca concluída: o app usa o BERTimbau fine-tunado, convertido para ONNX
 * e quantizado em INT8, carregado de `assets/model.onnx` com o vocabulário
 * em `assets/vocab.txt`. Nenhuma outra classe precisou mudar
 * (ver ClassifierRepository). A implementação simulada permanece em
 * [com.tcc.guardiaosms.classifier.MockClassifier] para testes de UI.
 */
class AppContainer(private val context: Context) {

    val database: AppDatabase by lazy { AppDatabase.getInstance(context) }

    val preferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }

    val classifierRepository: ClassifierRepository by lazy {
        OnnxClassifier(context)
    }

    val alertNotifier: AlertNotifier by lazy { AlertNotifier(context) }
}