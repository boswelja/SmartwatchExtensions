package com.boswelja.smartwatchextensions.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@ExperimentalMaterialApi
@Composable
fun SwitchSetting(
    text: String,
    secondaryText: String? = null,
    icon: ImageVector? = null,
    isEnabled: Boolean = true,
    isChecked: Boolean,
    onCheckChanged: (Boolean) -> Unit
) {
    ListItem(
        text = {
            Text(text = text)
        },
        secondaryText = if (secondaryText != null) { { Text(secondaryText) } } else null,
        icon = { if (icon != null) { Icon(icon, null) } },
        trailing = {
            Switch(
                checked = isChecked,
                onCheckedChange = null,
                enabled = isEnabled
            )
        },
        modifier = Modifier.clickable(enabled = isEnabled) {
            onCheckChanged(!isChecked)
        }
    )
}

@ExperimentalMaterialApi
@Composable
fun CheckboxSetting(
    text: String,
    secondaryText: String? = null,
    icon: ImageVector? = null,
    isEnabled: Boolean = true,
    isChecked: Boolean,
    onCheckChanged: (Boolean) -> Unit
) {
    ListItem(
        text = { Text(text) },
        secondaryText = if (secondaryText != null) { { Text(secondaryText) } } else null,
        icon = { if (icon != null) { Icon(icon, null) } },
        trailing = {
            Checkbox(
                checked = isChecked,
                onCheckedChange = null,
                enabled = isEnabled
            )
        },
        modifier = Modifier.clickable(enabled = isEnabled) {
            onCheckChanged(!isChecked)
        }
    )
}

@ExperimentalMaterialApi
@Composable
fun SliderSetting(
    text: String,
    icon: ImageVector? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    value: Float,
    trailingFormat: String? = null,
    isEnabled: Boolean = true,
    onSliderValueChanged: (Float) -> Unit,
    onSliderValueFinished: () -> Unit
) {
    ListItem(
        text = { Text(text) },
        icon = { if (icon != null) { Icon(icon, null) } },
        secondaryText = {
            Slider(
                value = value,
                valueRange = valueRange,
                onValueChange = onSliderValueChanged,
                onValueChangeFinished = onSliderValueFinished,
                enabled = isEnabled
            )
        },
        trailing = if (trailingFormat != null) {
            { Text(trailingFormat.format((value * 100).toInt())) }
        } else null
    )
}

@ExperimentalMaterialApi
@Composable
fun <T> DialogSetting(
    text: String,
    secondaryText: String? = null,
    icon: ImageVector? = null,
    isEnabled: Boolean = true,
    values: List<T>,
    value: T,
    onValueChanged: (T) -> Unit,
    valueLabel: @Composable (T) -> Unit
) {
    var dialogVisible by remember { mutableStateOf(false) }
    ListItem(
        text = { Text(text) },
        secondaryText = if (secondaryText != null) { { Text(secondaryText) } } else null,
        icon = { if (icon != null) { Icon(icon, null) } },
        modifier = Modifier.clickable(enabled = isEnabled) { dialogVisible = true }
    )
    if (dialogVisible) {
        ConfirmationDialog(
            title = { Text(text) },
            onDismissRequest = { dialogVisible = false },
            itemContent = { item -> valueLabel(item) },
            items = values,
            selectedItem = value,
            onItemSelectionChanged = onValueChanged
        )
    }
}

@Composable
fun HeaderItem(text: String) {
    Box(Modifier.background(MaterialTheme.colors.background)) {
        Text(
            text,
            Modifier
                .fillMaxWidth()
                .padding(start = 72.dp, top = 16.dp, bottom = 8.dp, end = 8.dp),
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.primary
        )
    }
}
