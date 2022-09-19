package com.boswelja.smartwatchextensions.core.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Hero Setting is used to display a single checkable setting that controls other settings on the screen.
 * @param checked Whether the setting is currently checked.
 * @param onCheckedChange Called when the check state should change.
 * @param text The text to display for this setting. This should be one line, but is not required.
 * @param modifier [Modifier].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroSetting(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    )
    Card(
        modifier = Modifier
            .padding(SettingDefaults.Padding)
            .then(modifier),
        colors = cardColors(containerColor = containerColor),
        onClick = { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ProvideTextStyle(SettingDefaults.TitleTextStyle) {
                text()
            }
            Switch(checked = checked, onCheckedChange = null)
        }
    }
}
