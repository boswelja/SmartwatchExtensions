package com.boswelja.smartwatchextensions.common.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CardHeader(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null
) {
    Row(modifier = modifier.padding(16.dp)) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(16.dp))
        }
        Column {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.subtitle1
            ) {
                title()
            }
            if (subtitle != null) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.subtitle2
                ) {
                    subtitle()
                }
            }
        }
    }
}
