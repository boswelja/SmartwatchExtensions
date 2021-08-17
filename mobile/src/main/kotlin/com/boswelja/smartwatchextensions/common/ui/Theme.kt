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

private val Purple200 = Color(0xffce93d8)
private val Purple500 = Color(0xff9c27b0)
private val Purple50 = Color(0xfff0eaf1)
private val Purple10 = Color(0xfffdfbfe)
private val Purple900 = Color(0xff130e15)

private const val useMonet = true

@Composable
fun getColors(darkTheme: Boolean): Colors {
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
    return if (useMonet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(getColor(android.R.color.system_accent1_200))
    } else {
        Purple200
    }
}

private fun Context.getLightPrimaryColor(): Color {
    return if (useMonet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(getColor(android.R.color.system_accent1_500))
    } else {
        Purple500
    }
}

private fun Context.getLightBackgroundColor(): Color {
    return if (useMonet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(getColor(android.R.color.system_neutral1_50))
    } else {
        Purple50
    }
}

private fun Context.getLightSurfaceColor(): Color {
    return if (useMonet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(getColor(android.R.color.system_neutral1_10))
    } else {
        Purple10
    }
}

private fun Context.getDarkBackgroundColor(): Color {
    return if (useMonet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(getColor(android.R.color.system_neutral1_900))
    } else {
        Purple900
    }
}

private fun Context.getDarkSurfaceColor(): Color {
    return if (useMonet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(getColor(android.R.color.system_neutral1_900))
    } else {
        Purple900
    }
}

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
