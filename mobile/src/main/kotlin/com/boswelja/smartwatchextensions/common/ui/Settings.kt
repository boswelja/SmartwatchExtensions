package com.boswelja.smartwatchextensions.common.ui

import androidx.compose.foundation.clickable
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Slider
import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

/**
 * A setting to allow the user to check/uncheck a value.
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
fun CheckboxSetting(
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
            Checkbox(
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

/**
 * A setting to allow the user to select a value from a range in a slider.
 * @param modifier [Modifier].
 * @param label The setting label Composable.
 * @param icon The setting icon Composable.
 * @param trailing The trailing Composable. This can be used to display the current value.
 * @param valueRange The range of values selectable by the slider.
 * @param value The current slider value.
 * @param enabled Whether the setting is enabled.
 * @param onSliderValueChanged Called when the slider value changes.
 * @param onSliderValueFinished Called when the user stops adjusting the slider value.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SliderSetting(
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    trailing: @Composable ((Float) -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    value: Float,
    enabled: Boolean = true,
    onSliderValueChanged: (Float) -> Unit,
    onSliderValueFinished: () -> Unit
) {
    ListItem(
        modifier = modifier,
        text = label,
        icon = icon,
        secondaryText = {
            Slider(
                value = value,
                valueRange = valueRange,
                onValueChange = onSliderValueChanged,
                onValueChangeFinished = onSliderValueFinished,
                enabled = enabled
            )
        },
        trailing = trailing?.let {
            {
                it(value)
            }
        }
    )
}

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
@OptIn(ExperimentalMaterialApi::class)
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
    ListItem(
        text = label,
        secondaryText = summary,
        icon = icon,
        modifier = modifier.clickable(enabled = enabled) { dialogVisible = true }
    )
    if (dialogVisible) {
        ConfirmationDialog(
            modifier = dialogModifier,
            title = label,
            onDismissRequest = { dialogVisible = false },
            itemContent = { item -> valueLabel(item) },
            items = values,
            selectedItem = value,
            onItemSelectionChanged = onValueChanged
        )
    }
}
