package com.tcc.guardiaosms.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.tcc.guardiaosms.tips.AttentionPoint
import com.tcc.guardiaosms.ui.components.AttentionPointsSection
import com.tcc.guardiaosms.ui.components.MinTouchHeight
import com.tcc.guardiaosms.ui.components.RiskBanner
import com.tcc.guardiaosms.ui.components.SafetyGuideSection
import com.tcc.guardiaosms.ui.util.formatDateTime

@Composable
fun AlertDetailScreen(viewModel: AlertDetailViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        DetailHeader(onBack)

        when (val s = state) {
            AlertDetailUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            AlertDetailUiState.NotFound -> Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(stringResource(R.string.detail_not_found), style = MaterialTheme.typography.bodyLarge)
                BackButton(onBack)
            }

            is AlertDetailUiState.Loaded -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                RiskBanner(s.riskLevel)
                MessageCard(
                    sender = s.record.sender,
                    receivedAt = formatDateTime(s.record.receivedAtEpochMillis, stringResource(R.string.date_time_pattern)),
                    body = s.record.body
                )
                AttentionPointsSection(s.attentionPoints, s.riskLevel)
                HorizontalDivider()
                SafetyGuideSection(s.riskLevel, hasMoneyRequest = AttentionPoint.MONEY_REQUEST in s.attentionPoints)
                BackButton(onBack, Modifier.padding(bottom = 16.dp))
            }
        }
    }
}

@Composable
private fun DetailHeader(onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(56.dp)) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = stringResource(R.string.detail_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() }
        )
    }
}

@Composable
private fun MessageCard(sender: String, receivedAt: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LabeledValue(stringResource(R.string.detail_from), sender)
            LabeledValue(stringResource(R.string.detail_received_at), receivedAt)
            // Texto simples: links da mensagem NÃO ficam clicáveis, de propósito.
            LabeledValue(stringResource(R.string.detail_message), body)
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Column(modifier = Modifier.semantics(mergeDescendants = true) {}) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onBack,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = MinTouchHeight)
    ) {
        Text(stringResource(R.string.action_back))
    }
}
