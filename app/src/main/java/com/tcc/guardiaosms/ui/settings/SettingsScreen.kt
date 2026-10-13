package com.tcc.guardiaosms.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tcc.guardiaosms.R
import com.tcc.guardiaosms.data.preferences.ThemeMode
import com.tcc.guardiaosms.ui.components.MinTouchHeight
import com.tcc.guardiaosms.ui.components.SectionTitle
import com.tcc.guardiaosms.ui.util.formatDecimal

private val themeOptions = listOf(
    ThemeMode.SYSTEM to R.string.settings_theme_system,
    ThemeMode.LIGHT to R.string.settings_theme_light,
    ThemeMode.DARK to R.string.settings_theme_dark
)

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val threshold by viewModel.threshold.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
        )

        // --- Tema ---
        SectionTitle(stringResource(R.string.settings_theme_label))
        Column(modifier = Modifier.selectableGroup()) {
            themeOptions.forEach { (mode, labelRes) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = MinTouchHeight)
                        .selectable(
                            selected = themeMode == mode,
                            onClick = { viewModel.onThemeModeSelected(mode) },
                            role = Role.RadioButton
                        )
                ) {
                    RadioButton(selected = themeMode == mode, onClick = null)
                    Text(
                        text = stringResource(labelRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        HorizontalDivider()

        // --- Notificações ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MinTouchHeight)
                .toggleable(
                    value = notificationsEnabled,
                    onValueChange = { viewModel.onNotificationsToggled(it) },
                    role = Role.Switch
                )
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_notifications_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.settings_notifications_help),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = notificationsEnabled, onCheckedChange = null)
        }

        HorizontalDivider()

        // --- Sensibilidade (mesma faixa e mesma regra de antes) ---
        SectionTitle(stringResource(R.string.settings_sensitivity_label))
        Text(
            text = stringResource(R.string.settings_sensitivity_help),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.settings_sensitivity_value, formatDecimal(threshold)),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Column {
            Slider(
                value = threshold,
                onValueChange = { viewModel.onThresholdChanged(it) },
                valueRange = 0.05f..0.95f
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(R.string.settings_sensitivity_more),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(R.string.settings_sensitivity_less),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        OutlinedButton(
            onClick = { viewModel.onResetThreshold() },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MinTouchHeight)
        ) {
            Text(stringResource(R.string.settings_reset_default))
        }
    }
}
