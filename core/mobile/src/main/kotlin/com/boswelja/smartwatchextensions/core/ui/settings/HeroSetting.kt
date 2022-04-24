package com.boswelja.smartwatchextensions.core.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
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
    Card(
        modifier = Modifier
            .padding(SettingDefaults.Padding)
            .clickable { onCheckedChange(!checked) }
            .then(modifier),
        containerColor = MaterialTheme.colorScheme.primaryContainer
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
            Checkbox(
                checked = checked,
                onCheckedChange = null
            )
        }
    }
}
