package com.boswelja.smartwatchextensions.common.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.boswelja.smartwatchextensions.R

/**
 * A box styled for a dialog header. Padding, alignment and text style are applied automatically.
 * @param title The title text Composable.
 */
@Composable
fun DialogHeader(
    title: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .height(64.dp)
            .padding(start = 24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
            title()
        }
    }
}

/**
 * A row of buttons for a material dialog. Spacing is applied automatically.
 * @param positiveButton The positive button Composable.
 * @param negativeButton The negative button Composable. This is optional.
 * @param neutralButton The neutral button Composable. This is optional.
 */
@Composable
fun DialogButtons(
    positiveButton: @Composable () -> Unit,
    negativeButton: @Composable (() -> Unit)? = null,
    neutralButton: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        neutralButton?.invoke()
        Spacer(Modifier.weight(1f))
        negativeButton?.invoke()
        positiveButton()
    }
}

/**
 * A base material dialog with support for shape, colour, elevation.
 * @param modifier [Modifier].
 * @param backgroundColor The dialog background color.
 * @param contentColor The dialog content color.
 * @param dialogProperties [DialogProperties].
 * @param onDismissRequest Called when the dialog should be dismissed.
 * @param content The dialog content.
 */
@Composable
fun MaterialDialog(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    dialogProperties: DialogProperties = DialogProperties(),
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = dialogProperties
    ) {
        Surface(
            modifier = modifier.heightIn(max = 560.dp),
            color = backgroundColor,
            contentColor = contentColor,
            content = content
        )
    }
}

/**
 * A material dialog for selecting an option from a list of available options.
 * @param modifier [Modifier].
 * @param title The dialog title.
 * @param onDismissRequest Called when the dialog should be dismissed.
 * @param itemContent The item content Composable. This content automatically has a RadioButton
 * added to the start of the layout.
 * @param items The list of available items.
 * @param selectedItem The current selected item.
 * @param onItemSelectionChanged Called when a new item is selected.
 * @param backgroundColor The dialog background color.
 * @param contentColor The dialog content color.
 * @param dialogProperties [DialogProperties].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ConfirmationDialog(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    onDismissRequest: () -> Unit,
    itemContent: @Composable RowScope.(T) -> Unit,
    items: List<T>,
    selectedItem: T,
    onItemSelectionChanged: (T) -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    dialogProperties: DialogProperties = DialogProperties()
) {
    var dialogSelectedItem by remember(selectedItem) {
        mutableStateOf(selectedItem)
    }

    MaterialDialog(
        modifier = modifier,
        contentColor = contentColor,
        backgroundColor = backgroundColor,
        dialogProperties = dialogProperties,
        onDismissRequest = onDismissRequest
    ) {
        Column {
            DialogHeader(title = title)
            Divider()
            LazyColumn {
                items(items) { item ->
                    Row(
                        modifier = Modifier
                            .requiredHeight(48.dp)
                            .fillMaxWidth()
                            .clickable { dialogSelectedItem = item },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(24.dp))
                        RadioButton(selected = item == dialogSelectedItem, onClick = null)
                        Spacer(Modifier.width(32.dp))
                        itemContent(item)
                    }
                }
            }
            Divider()
            DialogButtons(
                positiveButton = {
                    TextButton(
                        onClick = {
                            onItemSelectionChanged(dialogSelectedItem)
                            onDismissRequest()
                        }
                    ) {
                        Text(stringResource(R.string.button_done))
                    }
                },
                negativeButton = {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.dialog_button_cancel))
                    }
                }
            )
        }
    }
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
