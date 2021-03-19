package com.boswelja.devicemanager.common.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
            Switch(checked = isChecked, onCheckedChange = onCheckChanged)
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
            Checkbox(checked = isChecked, onCheckedChange = onCheckChanged)
        }
    )
}

@ExperimentalMaterialApi
@Preview(showBackground = true, showSystemUi = false)
@Composable
fun SwitchPreferencePreview() {
    SwitchPreference(
        text = "Preference",
        secondaryText = "Secondary line",
        isChecked = true,
        onCheckChanged = {
        }
    )
}

@ExperimentalMaterialApi
@Preview(showBackground = true, showSystemUi = false)
@Composable
fun CheckboxPreferencePreview() {
    CheckboxPreference(
        text = "Preference",
        secondaryText = "Secondary line",
        isChecked = true,
        onCheckChanged = {
        }
    )
}
