package com.boswelja.smartwatchextensions.core.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A setting to allow the user to switch on/off a value.
 * @param modifier [Modifier].
 * @param label The setting label Composable. This will be applied to the setting item and dialog.
 * @param summary The setting summary Composable. This will be applied to the setting item.
 * @param icon The setting icon Composable. This will be applied to the setting item.
 * @param enabled Whether the setting is enabled.
 * @param checked Whether the setting is checked.
 * @param onCheckChanged Called when the checked value changes.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwitchSetting(
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
    summary: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    checked: Boolean,
    onCheckChanged: (Boolean) -> Unit
) {
    ListItem(
        text = label,
        secondaryText = summary,
        icon = icon,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled
            )
        },
        modifier = modifier.clickable(enabled = enabled) {
            onCheckChanged(!checked)
        }
    )
}
