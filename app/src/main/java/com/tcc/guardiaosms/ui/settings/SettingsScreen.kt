package com.tcc.guardiaosms.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tcc.guardiaosms.R

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val threshold by viewModel.threshold.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = stringResource(R.string.settings_sensitivity_label) + ": %.2f".format(threshold),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = stringResource(R.string.settings_sensitivity_help),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )
        Slider(
            value = threshold,
            onValueChange = { viewModel.onThresholdChanged(it) },
            valueRange = 0.05f..0.95f
        )
        OutlinedButton(
            onClick = { viewModel.onResetThreshold() },
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        ) {
            Text(stringResource(R.string.settings_reset_default))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_notifications_label),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { viewModel.onNotificationsToggled(it) }
            )
        }
    }
}
