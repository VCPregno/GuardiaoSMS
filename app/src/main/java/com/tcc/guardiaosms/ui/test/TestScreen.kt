package com.tcc.guardiaosms.ui.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tcc.guardiaosms.R
import com.tcc.guardiaosms.ui.components.AttentionPointsSection
import com.tcc.guardiaosms.ui.components.MinTouchHeight
import com.tcc.guardiaosms.ui.components.RiskBadge
import com.tcc.guardiaosms.ui.components.SectionTitle
import com.tcc.guardiaosms.ui.theme.riskColors
import com.tcc.guardiaosms.ui.util.formatProbabilityPercent
import com.tcc.guardiaosms.ui.util.formatThresholdPercent

/**
 * Exemplos de demonstração (rótulo, texto), em strings.xml.
 *
 * Escolhidos para cobrir os dois extremos, cobrindo os padrões de smishing
 * descritos no TCC (falso bloqueio de conta, cobrança falsa via Pix, link
 * encurtado) e mensagens legítimas cotidianas. Servem para a apresentação à
 * banca não depender de digitação na hora.
 */
private val EXAMPLE_MESSAGES = listOf(
    R.string.test_example_1_label to R.string.test_example_1_text,
    R.string.test_example_2_label to R.string.test_example_2_text,
    R.string.test_example_3_label to R.string.test_example_3_text,
    R.string.test_example_4_label to R.string.test_example_4_text
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
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.test_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(R.string.test_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = { viewModel.onTextChanged(it) },
            label = { Text(stringResource(R.string.test_input_label)) },
            placeholder = { Text(stringResource(R.string.test_input_placeholder)) },
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp)
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
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = MinTouchHeight)
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(stringResource(R.string.test_analyzing), modifier = Modifier.padding(start = 8.dp))
                } else {
                    Text(stringResource(R.string.test_analyze_button))
                }
            }
            OutlinedButton(
                onClick = { viewModel.onClear() },
                modifier = Modifier.heightIn(min = MinTouchHeight)
            ) {
                Text(stringResource(R.string.test_clear_button))
            }
        }

        result?.let { ResultCard(it) }

        SectionTitle(
            stringResource(R.string.test_examples_label),
            modifier = Modifier.padding(top = 32.dp, bottom = 8.dp)
        )
        EXAMPLE_MESSAGES.forEach { (labelRes, textRes) ->
            val message = stringResource(textRes)
            OutlinedButton(
                onClick = { viewModel.onTextChanged(message) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .heightIn(min = MinTouchHeight)
            ) {
                Text(stringResource(labelRes))
            }
        }
    }
}

@Composable
private fun ResultCard(result: ManualTestResult) {
    val colors = MaterialTheme.riskColors.forLevel(result.riskLevel)

    Column(
        modifier = Modifier.padding(top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RiskBadge(result.riskLevel)

                Text(
                    text = stringResource(
                        R.string.test_result_probability,
                        formatProbabilityPercent(result.scamProbability)
                    ),
                    style = MaterialTheme.typography.titleMedium
                )

                // Barra visual: ajuda o público não técnico (e a banca) a enxergar
                // a distância entre a probabilidade e o limiar de decisão.
                // Implementada com Box em vez de LinearProgressIndicator porque a
                // assinatura desse componente mudou entre versões do Material3;
                // assim o código não depende da versão exata resolvida pelo BOM.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(result.scamProbability.coerceIn(0f, 1f))
                            .height(12.dp)
                            .background(colors.accent, RoundedCornerShape(6.dp))
                    )
                }

                Text(
                    text = stringResource(R.string.test_result_threshold, formatThresholdPercent(result.thresholdUsed)),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AttentionPointsSection(result.attentionPoints, result.riskLevel)
    }
}
