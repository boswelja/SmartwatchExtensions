package com.boswelja.smartwatchextensions.core.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.boswelja.smartwatchextensions.core.ui.theme.DisabledAlpha

/**
 * Shortcut setting is used to display an item on a settings screen that links into a deeper settings menu.
 * @param text The text to display. This should describe the shortcut.
 * @param onClick Called when the setting is clicked.
 * @param modifier [Modifier].
 * @param enabled Whether the shortcut is enabled. If false, interaction is disabled.
 * @param summary The summary to display. This should describe the state of the destination where possible.
 */
@Composable
fun ShortcutSetting(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    summary: (@Composable () -> Unit)? = null
) {
    val textColor = if (enabled) {
        LocalContentColor.current
    } else {
        LocalContentColor.current.copy(alpha = DisabledAlpha)
    }
    Column(
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(SettingDefaults.Padding)
            .then(modifier)
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
}
