package com.boswelja.smartwatchextensions.core.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.core.ui.theme.DisabledAlpha

/**
 * Checkbox setting allows the user to choose one of two states, checked and unchecked.
 * @param checked Whether the setting is currently checked.
 * @param onCheckedChange Called when the check state should change.
 * @param text The text to display. This should describe what the checkbox does.
 * @param modifier [Modifier].
 * @param enabled Whether this setting is enabled. If false, interaction is disabled.
 * @param summary The summary of this setting. This should describe the setting state when possible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckboxSetting(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    summary: (@Composable () -> Unit)? = null
) {
    val textColor = if (enabled) {
        LocalContentColor.current
    } else {
        LocalContentColor.current.copy(alpha = DisabledAlpha)
    }
    Row(
        modifier = Modifier
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(SettingDefaults.Padding)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier.weight(1f)
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides SettingDefaults.TitleTextStyle,
                LocalContentColor provides textColor
            ) {
                text()
            }
            if (summary != null) {
                CompositionLocalProvider(
                    LocalTextStyle provides SettingDefaults.SummaryTextStyle,
                    LocalContentColor provides textColor
                ) {
                    summary()
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled
        )
    }
}
