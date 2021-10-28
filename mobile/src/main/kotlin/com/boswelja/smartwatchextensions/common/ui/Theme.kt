package com.boswelja.smartwatchextensions.common.ui

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private const val PURPLE_10 = 0xfffdfbfe
private const val PURPLE_50 = 0xfff0eaf1
private const val PURPLE_200 = 0xffce93d8
private const val PURPLE_500 = 0xff9c27b0
private const val PURPLE_900 = 0xff130e15

private const val MONET_ENABLED = true

@Composable
private fun getColors(darkTheme: Boolean): Colors {
    val context = LocalContext.current
    return remember(darkTheme) {
        if (darkTheme) {
            val darkPrimary = context.getDarkPrimaryColor()
            darkColors(
                primary = darkPrimary,
                primaryVariant = darkPrimary,
                secondary = darkPrimary,
                secondaryVariant = darkPrimary,
                background = context.getDarkBackgroundColor(),
                surface = context.getDarkSurfaceColor()
            )
        } else {
            val lightPrimary = context.getLightPrimaryColor()
            lightColors(
                primary = lightPrimary,
                primaryVariant = lightPrimary,
                secondary = lightPrimary,
                secondaryVariant = lightPrimary,
                background = context.getLightBackgroundColor(),
                surface = context.getLightSurfaceColor()
            )
        }
    }
}

private fun Context.getDarkPrimaryColor(): Color {
    return if (MONET_ENABLED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(getColor(android.R.color.system_accent1_200))
    } else {
        Color(PURPLE_200)
    }
}

private fun Context.getLightPrimaryColor(): Color {
    return if (MONET_ENABLED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(getColor(android.R.color.system_accent1_500))
    } else {
        Color(PURPLE_500)
    }
}

private fun Context.getLightBackgroundColor(): Color {
    return if (MONET_ENABLED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(getColor(android.R.color.system_neutral1_50))
    } else {
        Color(PURPLE_50)
    }
}

private fun Context.getLightSurfaceColor(): Color {
    return if (MONET_ENABLED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(getColor(android.R.color.system_neutral1_10))
    } else {
        Color(PURPLE_10)
    }
}

private fun Context.getDarkBackgroundColor(): Color {
    return if (MONET_ENABLED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(getColor(android.R.color.system_neutral1_900))
    } else {
        Color(PURPLE_900)
    }
}

private fun Context.getDarkSurfaceColor(): Color {
    return if (MONET_ENABLED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(getColor(android.R.color.system_neutral1_900))
    } else {
        Color(PURPLE_900)
    }
}

private val shapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp)
)

/**
 * Apples [MaterialTheme] with the app colours to the child Composables.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    MaterialTheme(
        colors = getColors(darkTheme = darkTheme),
        shapes = shapes
    ) {
        val systemBarColor = MaterialTheme.colors.surface
        LaunchedEffect(systemBarColor) {
            systemUiController.setSystemBarsColor(systemBarColor)
        }
        content()
    }
}
