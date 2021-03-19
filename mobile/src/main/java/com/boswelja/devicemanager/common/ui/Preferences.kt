package com.boswelja.devicemanager.common.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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
        icon = { if (icon != null) { Icon(icon, null, Modifier.size(40.dp)) } },
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
        icon = { if (icon != null) { Icon(icon, null, Modifier.size(40.dp)) } },
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
    onSliderValueChanged: (Float) -> Unit
) {
    ListItem(
        text = { Text(text) },
        icon = { if (icon != null) { Icon(icon, null, Modifier.size(40.dp)) } },
        secondaryText = {
            Slider(
                value = value / 100f,
                valueRange = valueRange,
                onValueChange = {
                    onSliderValueChanged(it)
                }
            )
        },
        trailing = {
            Text(
                "$value%"
            )
        }
    )
}

@ExperimentalMaterialApi
@Preview(showBackground = true, showSystemUi = false)
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
@Preview(showBackground = true, showSystemUi = false)
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
@Preview(showBackground = true, showSystemUi = false)
@Composable
fun SliderPreferencePreview() {
    var value by remember { mutableStateOf(.5f) }
    SliderPreference(
        text = "Preference",
        value = value,
        onSliderValueChanged = { value = it }
    )
}
