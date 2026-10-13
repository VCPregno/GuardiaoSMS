package com.tcc.guardiaosms.ui.home

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tcc.guardiaosms.R
import com.tcc.guardiaosms.data.local.SmsRecordEntity
import com.tcc.guardiaosms.risk.RiskLevel
import com.tcc.guardiaosms.ui.components.MinTouchHeight
import com.tcc.guardiaosms.ui.components.RiskBadge
import com.tcc.guardiaosms.ui.components.SectionTitle
import com.tcc.guardiaosms.ui.theme.riskColors
import com.tcc.guardiaosms.ui.util.formatDateTime

@Composable
fun HomeScreen(
    hasSmsPermission: Boolean,
    onRequestPermission: () -> Unit,
    onOpenRecord: (Long) -> Unit,
    viewModel: HomeViewModel
) {
    val lastScam by viewModel.lastScamRecord.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
        )

        StatusCard(hasSmsPermission, onRequestPermission)

        SectionTitle(stringResource(R.string.home_last_alert_title))
        val record = lastScam
        if (record == null) {
            Text(
                text = stringResource(R.string.home_last_alert_none),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            LastAlertCard(record, onClick = { onOpenRecord(record.id) })
        }
    }
}

@Composable
private fun StatusCard(hasSmsPermission: Boolean, onRequestPermission: () -> Unit) {
    val colors = if (hasSmsPermission) MaterialTheme.riskColors.safe else MaterialTheme.riskColors.attention
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.container, contentColor = colors.onContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.semantics(mergeDescendants = true) {}
            ) {
                Icon(
                    if (hasSmsPermission) Icons.Filled.Shield else Icons.Filled.Sms,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(48.dp)
                )
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = stringResource(
                            if (hasSmsPermission) R.string.home_status_protected else R.string.home_status_permission_needed
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(
                            if (hasSmsPermission) R.string.home_status_protected_detail else R.string.home_status_permission_detail
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (!hasSmsPermission) {
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .heightIn(min = MinTouchHeight)
                ) {
                    Text(stringResource(R.string.home_grant_permission))
                }
            }
        }
    }
}

@Composable
private fun LastAlertCard(record: SmsRecordEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                RiskBadge(RiskLevel.HIGH)
                Text(record.sender, style = MaterialTheme.typography.titleMedium)
                Text(
                    formatDateTime(record.receivedAtEpochMillis, stringResource(R.string.date_time_pattern)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    record.body,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    stringResource(R.string.home_open_alert_hint),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
