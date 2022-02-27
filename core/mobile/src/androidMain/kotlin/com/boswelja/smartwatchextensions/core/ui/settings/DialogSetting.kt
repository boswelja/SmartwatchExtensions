package com.boswelja.smartwatchextensions.core.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.boswelja.smartwatchextensions.core.ui.dialog.ConfirmationDialog

/**
 * A settings item that allows the user to select from a list of values offered in a dialog.
 * @param modifier A [Modifier] to apply to the setting item.
 * @param dialogModifier A [Modifier] to apply to the dialog.
 * @param label The setting label Composable. This will be applied to the setting item and dialog.
 * @param summary The setting summary Composable. This will be applied to the setting item.
 * @param icon The setting icon Composable. This will be applied to the setting item.
 * @param enabled Whether the setting is enabled. If disabled, clicks will not be registered.
 * @param values The list of values the user can choose from.
 * @param value The current value.
 * @param onValueChanged Called when the current value changes.
 * @param valueLabel The value label Composable. This will be used in the dialog.
 */
@Composable
fun <T> DialogSetting(
    modifier: Modifier = Modifier,
    dialogModifier: Modifier = Modifier,
    label: @Composable () -> Unit,
    summary: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    values: List<T>,
    value: T,
    onValueChanged: (T) -> Unit,
    valueLabel: @Composable (T) -> Unit
) {
    var dialogVisible by remember { mutableStateOf(false) }
    ShortcutSetting(
        text = label,
        summary = summary,
        onClick = { dialogVisible = true },
        enabled = enabled,
        modifier = modifier
    )
    if (dialogVisible) {
        ConfirmationDialog(
            modifier = dialogModifier,
            title = label,
            icon = icon,
            onDismissRequest = { dialogVisible = false },
            itemContent = { item -> valueLabel(item) },
            items = values,
            selectedItem = value,
            onItemSelectionChanged = onValueChanged
        )
    }
}
