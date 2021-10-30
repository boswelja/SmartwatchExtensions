package com.boswelja.smartwatchextensions.onboarding.ui

import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.boswelja.smartwatchextensions.R

/**
 * Displays a dialog informing the user their device may be incompatible.
 * @param modifier [Modifier].
 * @param visible Whether the dialog is visible.
 * @param onDismissRequest Called when the dialog is dismissed.
 */
@Composable
fun CompatibilityDialog(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onDismissRequest: () -> Unit
) {
    if (visible) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = onDismissRequest,
            title = {
                Text(stringResource(R.string.onboarding_not_compatible_title))
            },
            text = {
                Text(stringResource(R.string.onboarding_not_compatible_desc))
            },
            icon = {
                Icon(Icons.Default.Error, null)
            },
            confirmButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.button_finish))
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        )
    }
}
