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
import androidx.compose.material.Divider
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.boswelja.smartwatchextensions.R

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
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.h6,
            content = title
        )
    }
}

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

@Composable
fun MaterialDialog(
    modifier: Modifier = Modifier,
    elevation: Dp = 24.dp,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = MaterialTheme.colors.contentColorFor(backgroundColor),
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
            shape = shape,
            color = backgroundColor,
            contentColor = contentColor,
            elevation = elevation,
            content = content
        )
    }
}

@Composable
fun <T> ConfirmationDialog(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    onDismissRequest: () -> Unit,
    itemContent: @Composable RowScope.(T) -> Unit,
    items: List<T>,
    selectedItem: T,
    onItemSelectionChanged: (T) -> Unit,
    elevation: Dp = 24.dp,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = MaterialTheme.colors.contentColorFor(backgroundColor),
    dialogProperties: DialogProperties = DialogProperties()
) {
    var dialogSelectedItem by remember(selectedItem) {
        mutableStateOf(selectedItem)
    }

    MaterialDialog(
        modifier = modifier,
        elevation = elevation,
        shape = shape,
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
