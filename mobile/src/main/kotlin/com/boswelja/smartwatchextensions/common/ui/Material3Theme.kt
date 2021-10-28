package com.boswelja.smartwatchextensions.common.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private const val PURPLE_10 = 0xfffdfbfe
private const val PURPLE_50 = 0xfff0eaf1
private const val PURPLE_200 = 0xffce93d8
private const val PURPLE_500 = 0xff9c27b0
private const val PURPLE_900 = 0xff130e15

@Composable
private fun getColorScheme(darkTheme: Boolean): ColorScheme {
    val context = LocalContext.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context)
        else dynamicLightColorScheme(context)
    } else {
        if (darkTheme)
            darkColorScheme(
                primary = Color(PURPLE_200),
                background = Color(PURPLE_900),
                surface = Color(PURPLE_900)
            )
        else
            lightColorScheme(
                primary = Color(PURPLE_500),
                background = Color(PURPLE_50),
                surface = Color(PURPLE_10)
            )
    }
}

/**
 * Apples [MaterialTheme] with the app colours to the child Composables.
 */
@Composable
fun AppTheme3(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    MaterialTheme(
        colorScheme = getColorScheme(darkTheme = darkTheme),
    ) {
        val systemBarColor = MaterialTheme.colorScheme.surface
        LaunchedEffect(systemBarColor) {
            systemUiController.setSystemBarsColor(systemBarColor)
        }
        content()
    }
}
