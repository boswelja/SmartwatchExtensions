package com.boswelja.smartwatchextensions.common.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.LocalContentColor
import androidx.wear.compose.material.LocalTextStyle
import androidx.wear.compose.material.MaterialTheme

@Composable
fun ExtensionCard(
    modifier: Modifier = Modifier,
    icon: @Composable (size: Dp) -> Unit,
    content: @Composable () -> Unit,
    onClick: () -> Unit,
    hintText: (@Composable () -> Unit)?,
    enabled: Boolean = true
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colors.onSurface
            ) {
                icon(48.dp)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                hintText?.let {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colors.onSurfaceVariant,
                        LocalTextStyle provides MaterialTheme.typography.caption1,
                        content = hintText
                    )
                }
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colors.onSurface,
                    LocalTextStyle provides MaterialTheme.typography.display3,
                    content = content
                )
            }
        }
    }
}
