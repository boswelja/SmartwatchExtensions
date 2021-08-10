package com.boswelja.smartwatchextensions.common.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BigButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = MaterialTheme.colors.primary,
    contentPadding: Dp = 16.dp,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Column(
            modifier = Modifier
                .clickable(onClick = onClick, enabled = enabled)
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            icon()
            text()
        }
    }
}
