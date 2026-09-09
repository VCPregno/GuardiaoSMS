package com.tcc.guardiaosms.ui.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tcc.guardiaosms.R
import com.tcc.guardiaosms.ui.theme.AlertDanger
import com.tcc.guardiaosms.ui.theme.AlertDangerBackground
import com.tcc.guardiaosms.ui.theme.AlertSafe
import com.tcc.guardiaosms.ui.theme.AlertSafeBackground

/**
 * Exemplos de demonstração.
 *
 * Escolhidos para cobrir os dois extremos e um caso ambíguo, cobrindo os
 * padrões de smishing descritos no TCC (falso bloqueio de conta, cobrança
 * falsa via Pix, link encurtado) e mensagens legítimas cotidianas.
 * Servem para a apresentação à banca não depender de digitação na hora.
 */
private val EXAMPLE_MESSAGES = listOf(
    "Golpe: falso bloqueio" to
        "URGENTE: sua conta sera bloqueada hoje. Regularize seu CPF agora: bit.ly/regularizar",
    "Golpe: cobrança falsa" to
        "Detectamos um Pix nao autorizado. Confirme seus dados para cancelar: gg.gg/pix-cancelar",
    "Legítima: pessoal" to
        "Oi mae, cheguei bem em casa. Te ligo mais tarde!",
    "Legítima: comercial" to
        "Seu pedido foi enviado e chega ate amanha. Obrigado pela preferencia!"
)

@Composable
fun TestScreen(viewModel: TestViewModel) {
    val inputText by viewModel.inputText.collectAsState()
    val result by viewModel.result.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.test_title),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(R.string.test_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = { viewModel.onTextChanged(it) },
            label = { Text(stringResource(R.string.test_input_label)) },
            placeholder = { Text(stringResource(R.string.test_input_placeholder)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.onAnalyze() },
                enabled = inputText.isNotBlank() && !isAnalyzing,
                modifier = Modifier.weight(1f)
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.test_analyze_button))
                }
            }
            OutlinedButton(onClick = { viewModel.onClear() }) {
                Text(stringResource(R.string.test_clear_button))
            }
        }

        result?.let { ResultCard(it) }

        Text(
            text = stringResource(R.string.test_examples_label),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 32.dp, bottom = 8.dp)
        )
        EXAMPLE_MESSAGES.forEach { (label, message) ->
            OutlinedButton(
                onClick = { viewModel.onTextChanged(message) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(label)
            }
        }
    }
}

@Composable
private fun ResultCard(result: ManualTestResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isScam) AlertDangerBackground else AlertSafeBackground
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = if (result.isScam)
                    stringResource(R.string.test_result_scam)
                else
                    stringResource(R.string.test_result_legit),
                style = MaterialTheme.typography.titleMedium,
                color = if (result.isScam) AlertDanger else AlertSafe
            )

            Text(
                text = stringResource(
                    R.string.test_result_probability,
                    result.scamProbability * 100
                ),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 12.dp)
            )

            // Barra visual: ajuda o público não técnico (e a banca) a enxergar
            // a distância entre a probabilidade e o limiar de decisão.
            // Implementada com Box em vez de LinearProgressIndicator porque a
            // assinatura desse componente mudou entre versões do Material3;
            // assim o código não depende da versão exata resolvida pelo BOM.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(10.dp)
                    .background(Color.LightGray, RoundedCornerShape(5.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(result.scamProbability.coerceIn(0f, 1f))
                        .height(10.dp)
                        .background(
                            if (result.isScam) AlertDanger else AlertSafe,
                            RoundedCornerShape(5.dp)
                        )
                )
            }

            Text(
                text = stringResource(
                    R.string.test_result_threshold,
                    result.thresholdUsed * 100
                ),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}
