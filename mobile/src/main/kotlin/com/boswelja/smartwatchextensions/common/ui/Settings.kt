package com.boswelja.smartwatchextensions.common.ui

import androidx.compose.foundation.clickable
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Slider
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SliderSetting(
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    value: Float,
    trailingFormat: String? = null,
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
        trailing = if (trailingFormat != null) {
            { Text(trailingFormat.format((value * 100).toInt())) }
        } else null
    )
}

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
