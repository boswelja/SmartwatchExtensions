package com.boswelja.smartwatchextensions.core.ui.list

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * An item to be displayed in a list.
 */
@Deprecated(
    message = "Please switch to Material3 ListItem instead",
    replaceWith = ReplaceWith(
        expression = "ListItem(headlineText = text, supportingText = secondaryText, leadingContent = icon, trailingContent = trailing, modifier = modifier)",
        imports = arrayOf("androidx.compose.material3.ListItem")
    )
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListItem(
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    secondaryText: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    androidx.compose.material3.ListItem(
        headlineText = text,
        supportingText = secondaryText,
        leadingContent = icon,
        trailingContent = trailing,
        modifier = modifier
    )
}
