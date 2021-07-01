package com.boswelja.smartwatchextensions.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Card

@Composable
fun ExtensionCard(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit,
    onClick: () -> Unit,
    hintText: @Composable () -> Unit = { },
    enabled: Boolean = true
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
    ) {
        Column {
            Row {
                icon()
                hintText()
            }
            content()
        }
    }
}
