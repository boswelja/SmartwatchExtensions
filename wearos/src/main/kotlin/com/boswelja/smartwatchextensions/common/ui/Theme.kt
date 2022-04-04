package com.boswelja.smartwatchextensions.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private const val Purple200 = 0xffce93d8

private val DarkColors = Colors(
    primary = Color(Purple200)
)

/**
 * Applies a [MaterialTheme] with our colours to Composables.
 */
@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = DarkColors,
        content = content
    )
}
