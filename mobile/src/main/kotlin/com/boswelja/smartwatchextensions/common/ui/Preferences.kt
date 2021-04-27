package com.boswelja.smartwatchextensions.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Slider
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R

@ExperimentalMaterialApi
@Composable
fun SwitchPreference(
    text: String,
    secondaryText: String? = null,
    icon: ImageVector? = null,
    isChecked: Boolean,
    onCheckChanged: (Boolean) -> Unit
) {
    ListItem(
        text = { Text(text) },
        secondaryText = if (secondaryText != null) { { Text(secondaryText) } } else null,
        icon = { if (icon != null) { Icon(icon, null) } },
        trailing = {
            Switch(checked = isChecked, onCheckedChange = null)
        },
        modifier = Modifier.clickable {
            onCheckChanged(!isChecked)
        }
    )
}

@ExperimentalMaterialApi
@Composable
fun CheckboxPreference(
    text: String,
    secondaryText: String? = null,
    icon: ImageVector? = null,
    isChecked: Boolean,
    onCheckChanged: (Boolean) -> Unit
) {
    ListItem(
        text = { Text(text) },
        secondaryText = if (secondaryText != null) { { Text(secondaryText) } } else null,
        icon = { if (icon != null) { Icon(icon, null) } },
        trailing = {
            Checkbox(checked = isChecked, onCheckedChange = null)
        },
        modifier = Modifier.clickable {
            onCheckChanged(!isChecked)
        }
    )
}

@ExperimentalMaterialApi
@Composable
fun SliderPreference(
    text: String,
    icon: ImageVector? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    value: Float,
    trailingFormat: String? = null,
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
                onValueChangeFinished = onSliderValueFinished
            )
        },
        trailing = if (trailingFormat != null) {
            { Text(trailingFormat.format((value * 100).toInt())) }
        } else null
    )
}

@ExperimentalMaterialApi
@Composable
fun <T> DialogPreference(
    text: String,
    secondaryText: String? = null,
    icon: ImageVector? = null,
    values: Array<Pair<String, T>>,
    value: Pair<String, T>,
    onValueChanged: (Pair<String, T>) -> Unit
) {
    var dialogVisible by remember { mutableStateOf(false) }
    ListItem(
        text = { Text(text) },
        secondaryText = if (secondaryText != null) { { Text(secondaryText) } } else null,
        icon = { if (icon != null) { Icon(icon, null) } },
        modifier = Modifier.clickable { dialogVisible = true }
    )
    if (dialogVisible) {
        var selectedValue by remember { mutableStateOf(value) }
        AlertDialog(
            title = { Text(text) },
            text = {
                LazyColumn {
                    items(values) { item ->
                        ListItem(
                            text = { Text(item.first) },
                            icon = {
                                RadioButton(
                                    selected = item.second == selectedValue.second,
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable {
                                selectedValue = item
                            }
                        )
                    }
                }
            },
            onDismissRequest = { dialogVisible = false },
            confirmButton = {
                TextButton(
                    content = { Text(stringResource(R.string.button_done)) },
                    onClick = {
                        dialogVisible = false
                        onValueChanged(selectedValue)
                    }
                )
            },
            dismissButton = {
                TextButton(
                    content = { Text(stringResource(R.string.dialog_button_cancel)) },
                    onClick = {
                        dialogVisible = false
                    }
                )
            }
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

@Preview(showBackground = true)
@Composable
fun HeaderItemPreview() {
    HeaderItem("Header")
}

@ExperimentalMaterialApi
@Preview(showBackground = true)
@Composable
fun SwitchPreferencePreview() {
    var value by remember { mutableStateOf(true) }
    SwitchPreference(
        text = "Preference",
        secondaryText = "Secondary line",
        isChecked = value,
        onCheckChanged = { value = it }
    )
}

@ExperimentalMaterialApi
@Preview(showBackground = true)
@Composable
fun CheckboxPreferencePreview() {
    var value by remember { mutableStateOf(true) }
    CheckboxPreference(
        text = "Preference",
        secondaryText = "Secondary line",
        isChecked = value,
        onCheckChanged = { value = it }
    )
}

@ExperimentalMaterialApi
@Preview(showBackground = true)
@Composable
fun SliderPreferencePreview() {
    var value by remember { mutableStateOf(.5f) }
    SliderPreference(
        text = "Preference",
        value = value,
        onSliderValueChanged = { value = it },
        onSliderValueFinished = { }
    )
}
