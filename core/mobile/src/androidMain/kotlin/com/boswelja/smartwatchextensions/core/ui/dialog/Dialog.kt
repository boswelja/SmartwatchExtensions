package com.boswelja.smartwatchextensions.core.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.core.R

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
    icon: @Composable (() -> Unit)? = null
) {
    var dialogSelectedItem by remember(selectedItem) {
        mutableStateOf(selectedItem)
    }

    AlertDialog(
        modifier = modifier,
        title = title,
        icon = icon,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onItemSelectionChanged(dialogSelectedItem)
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.dialog_button_done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_button_cancel))
            }
        },
        text = {
            Column {
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
            }
        }
    )
}
