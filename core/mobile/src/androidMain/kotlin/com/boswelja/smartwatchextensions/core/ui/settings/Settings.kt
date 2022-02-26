package com.boswelja.smartwatchextensions.core.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckboxSetting(
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
    summary: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    checked: Boolean,
    onCheckChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(enabled = enabled) {
                onCheckChanged(!checked)
            }
            .then(modifier)
    ) {
        icon?.invoke()
        Column {
            label()
            summary?.invoke()
        }
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled
        )
    }
}
