package com.tcc.guardiaosms.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tcc.guardiaosms.R
import com.tcc.guardiaosms.ui.theme.AlertDangerBackground
import com.tcc.guardiaosms.ui.theme.AlertSafe
import com.tcc.guardiaosms.ui.theme.AlertSafeBackground

@Composable
fun HomeScreen(
    hasSmsPermission: Boolean,
    onRequestPermission: () -> Unit,
    viewModel: HomeViewModel
) {
    val lastScam by viewModel.lastScamRecord.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (hasSmsPermission) AlertSafeBackground else AlertDangerBackground
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (hasSmsPermission)
                        stringResource(R.string.home_status_protected)
                    else
                        stringResource(R.string.home_status_permission_needed),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (hasSmsPermission) AlertSafe else MaterialTheme.colorScheme.error
                )

                if (!hasSmsPermission) {
                    Button(
                        onClick = onRequestPermission,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(stringResource(R.string.home_grant_permission))
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.home_last_alert_prefix),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = lastScam?.let { "${it.sender}: ${it.body.take(120)}" }
                        ?: stringResource(R.string.home_last_alert_none),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
