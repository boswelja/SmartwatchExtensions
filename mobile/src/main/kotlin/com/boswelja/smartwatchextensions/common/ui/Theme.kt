package com.boswelja.smartwatchextensions.common.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val Purple200 = Color(0xffd05ce3)
private val Purple500 = Color(0xff9c27b0)

private val DarkColors = darkColors(
    primary = Purple200,
    primaryVariant = Purple200,
    secondary = Purple200,
    secondaryVariant = Purple200
)
private val LightColors = lightColors(
    primary = Purple500,
    primaryVariant = Purple500,
    secondary = Purple500,
    secondaryVariant = Purple500
)

private val shapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        shapes = shapes,
        content = content
    )
}
