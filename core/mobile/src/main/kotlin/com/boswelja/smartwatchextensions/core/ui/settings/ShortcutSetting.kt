package com.boswelja.smartwatchextensions.core.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Shortcut setting is used to display an item on a settings screen that links into a deeper settings menu.
 * @param text The text to display. This should describe the shortcut.
 * @param onClick Called when the setting is clicked.
 * @param modifier [Modifier].
 * @param enabled Whether the shortcut is enabled. If false, interaction is disabled.
 * @param summary The summary to display. This should describe the state of the destination where possible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutSetting(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    summary: (@Composable () -> Unit)? = null
) {
    ListItem(
        headlineContent = text,
        supportingContent = summary,
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick).then(modifier)
    )
}
