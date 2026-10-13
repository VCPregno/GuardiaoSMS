package com.tcc.guardiaosms.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tcc.guardiaosms.R
import com.tcc.guardiaosms.risk.RiskLevel
import com.tcc.guardiaosms.tips.AttentionPoint
import com.tcc.guardiaosms.ui.theme.riskColors

/** Altura mínima dos botões e linhas clicáveis (acima dos 48dp recomendados). */
val MinTouchHeight = 56.dp

@get:StringRes
val RiskLevel.labelRes: Int
    get() = when (this) {
        RiskLevel.HIGH -> R.string.risk_high
        RiskLevel.ATTENTION -> R.string.risk_attention
        RiskLevel.LOW -> R.string.risk_low
    }

@get:StringRes
val RiskLevel.explanationRes: Int
    get() = when (this) {
        RiskLevel.HIGH -> R.string.risk_high_explanation
        RiskLevel.ATTENTION -> R.string.risk_attention_explanation
        RiskLevel.LOW -> R.string.risk_low_explanation
    }

val RiskLevel.icon: ImageVector
    get() = when (this) {
        RiskLevel.HIGH -> Icons.Filled.Warning
        RiskLevel.ATTENTION -> Icons.Filled.Info
        RiskLevel.LOW -> Icons.Filled.CheckCircle
    }

/** Título de seção, marcado como "heading" para leitores de tela. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.semantics { heading() }
    )
}

/** Selo pequeno do nível de risco: ícone + texto (não depende só da cor). */
@Composable
fun RiskBadge(level: RiskLevel, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.riskColors.forLevel(level)
    Surface(
        color = colors.container,
        contentColor = colors.onContainer,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(level.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
            Text(
                text = stringResource(level.labelRes),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

/** Faixa grande do nível de risco, com frase explicativa. */
@Composable
fun RiskBanner(level: RiskLevel, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.riskColors.forLevel(level)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.container, contentColor = colors.onContainer)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp)
        ) {
            Icon(level.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(48.dp))
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = stringResource(level.labelRes),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = stringResource(level.explanationRes),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private val AttentionPoint.labelRes: Int
    get() = when (this) {
        AttentionPoint.LINK -> R.string.tip_link
        AttentionPoint.MONEY_REQUEST -> R.string.tip_money
        AttentionPoint.DATA_REQUEST -> R.string.tip_data
        AttentionPoint.URGENCY_OR_THREAT -> R.string.tip_urgency
        AttentionPoint.PHONE_CONTACT -> R.string.tip_phone
        AttentionPoint.DISGUISED_TEXT -> R.string.tip_disguised
    }

private val AttentionPoint.icon: ImageVector
    get() = when (this) {
        AttentionPoint.LINK -> Icons.Filled.Link
        AttentionPoint.MONEY_REQUEST -> Icons.Filled.AttachMoney
        AttentionPoint.DATA_REQUEST -> Icons.Filled.Lock
        AttentionPoint.URGENCY_OR_THREAT -> Icons.Filled.AccessTime
        AttentionPoint.PHONE_CONTACT -> Icons.Filled.Phone
        AttentionPoint.DISGUISED_TEXT -> Icons.Filled.VisibilityOff
    }

/**
 * Pontos de atenção (dicas de leitura). Deixa explícito, com a nota no fim,
 * que as dicas NÃO são o motivo da decisão do modelo.
 */
@Composable
fun AttentionPointsSection(points: Set<AttentionPoint>, level: RiskLevel, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(
            stringResource(
                if (level == RiskLevel.LOW && points.isNotEmpty()) R.string.tips_title_safe else R.string.tips_title
            )
        )
        if (points.isEmpty()) {
            Text(
                text = stringResource(R.string.tips_none),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            points.forEach { point ->
                IconRow(icon = point.icon, text = stringResource(point.labelRes), tint = MaterialTheme.riskColors.attention.accent)
            }
        }
        Text(
            text = stringResource(R.string.tips_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private val guideItems = listOf(
    Icons.Filled.Block to R.string.guide_no_links,
    Icons.Filled.Block to R.string.guide_no_data,
    Icons.Filled.Block to R.string.guide_no_pix,
    Icons.Filled.Block to R.string.guide_no_calls,
    Icons.Filled.VerifiedUser to R.string.guide_confirm,
    Icons.Filled.People to R.string.guide_ask_help
)

/**
 * Guia fixo "O que fazer". Quando há pedido de dinheiro, destaca antes o
 * cuidado de confirmar com a pessoa pelo número antigo (golpe do falso parente).
 */
@Composable
fun SafetyGuideSection(level: RiskLevel, hasMoneyRequest: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(stringResource(if (level == RiskLevel.LOW) R.string.guide_title_safe else R.string.guide_title))

        if (hasMoneyRequest) {
            val colors = MaterialTheme.riskColors.attention
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {},
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.container, contentColor = colors.onContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Filled.Phone, contentDescription = null, tint = colors.accent, modifier = Modifier.size(32.dp))
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(stringResource(R.string.guide_money_highlight_title), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.guide_money_highlight),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        guideItems.forEach { (icon, textRes) ->
            IconRow(icon = icon, text = stringResource(textRes), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun IconRow(icon: ImageVector, text: String, tint: androidx.compose.ui.graphics.Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
