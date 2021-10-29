@file:Suppress("MagicNumber")

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

private val md_theme_light_primary = Color(0xFF9a25ae)
private val md_theme_light_onPrimary = Color(0xFFffffff)
private val md_theme_light_primaryContainer = Color(0xFFffd5ff)
private val md_theme_light_onPrimaryContainer = Color(0xFF350040)
private val md_theme_light_secondary = Color(0xFF6b586b)
private val md_theme_light_onSecondary = Color(0xFFffffff)
private val md_theme_light_secondaryContainer = Color(0xFFf5dbf2)
private val md_theme_light_onSecondaryContainer = Color(0xFF251626)
private val md_theme_light_tertiary = Color(0xFF82524a)
private val md_theme_light_onTertiary = Color(0xFFffffff)
private val md_theme_light_tertiaryContainer = Color(0xFFffdad2)
private val md_theme_light_onTertiaryContainer = Color(0xFF32110c)
private val md_theme_light_error = Color(0xFFba1b1b)
private val md_theme_light_errorContainer = Color(0xFFffdad4)
private val md_theme_light_onError = Color(0xFFffffff)
private val md_theme_light_onErrorContainer = Color(0xFF410001)
private val md_theme_light_background = Color(0xFFfcfcfc)
private val md_theme_light_onBackground = Color(0xFF1e1a1d)
private val md_theme_light_surface = Color(0xFFfcfcfc)
private val md_theme_light_onSurface = Color(0xFF1e1a1d)
private val md_theme_light_surfaceVariant = Color(0xFFecdee8)
private val md_theme_light_onSurfaceVariant = Color(0xFF4d444c)
private val md_theme_light_outline = Color(0xFF7e747c)
private val md_theme_light_inverseOnSurface = Color(0xFFf7eef3)
private val md_theme_light_inverseSurface = Color(0xFF332f32)

private val md_theme_dark_primary = Color(0xFFfbaaff)
private val md_theme_dark_onPrimary = Color(0xFF570068)
private val md_theme_dark_primaryContainer = Color(0xFF7b0091)
private val md_theme_dark_onPrimaryContainer = Color(0xFFffd5ff)
private val md_theme_dark_secondary = Color(0xFFd8bfd5)
private val md_theme_dark_onSecondary = Color(0xFF3b2b3b)
private val md_theme_dark_secondaryContainer = Color(0xFF534153)
private val md_theme_dark_onSecondaryContainer = Color(0xFFf5dbf2)
private val md_theme_dark_tertiary = Color(0xFFf6b8ae)
private val md_theme_dark_onTertiary = Color(0xFF4c251f)
private val md_theme_dark_tertiaryContainer = Color(0xFF663b34)
private val md_theme_dark_onTertiaryContainer = Color(0xFFffdad2)
private val md_theme_dark_error = Color(0xFFffb4a9)
private val md_theme_dark_errorContainer = Color(0xFF930006)
private val md_theme_dark_onError = Color(0xFF680003)
private val md_theme_dark_onErrorContainer = Color(0xFFffdad4)
private val md_theme_dark_background = Color(0xFF1e1a1d)
private val md_theme_dark_onBackground = Color(0xFFe9e0e5)
private val md_theme_dark_surface = Color(0xFF1e1a1d)
private val md_theme_dark_onSurface = Color(0xFFe9e0e5)
private val md_theme_dark_surfaceVariant = Color(0xFF4d444c)
private val md_theme_dark_onSurfaceVariant = Color(0xFFd0c3cc)
private val md_theme_dark_outline = Color(0xFF998e96)
private val md_theme_dark_inverseOnSurface = Color(0xFF1e1a1d)
private val md_theme_dark_inverseSurface = Color(0xFFe9e0e5)

private fun getColorScheme(darkTheme: Boolean): ColorScheme {
    return if (darkTheme) {
        darkColorScheme(
            primary = md_theme_dark_primary,
            onPrimary = md_theme_dark_onPrimary,
            primaryContainer = md_theme_dark_primaryContainer,
            onPrimaryContainer = md_theme_dark_onPrimaryContainer,
            secondary = md_theme_dark_secondary,
            onSecondary = md_theme_dark_onSecondary,
            secondaryContainer = md_theme_dark_secondaryContainer,
            onSecondaryContainer = md_theme_dark_onSecondaryContainer,
            tertiary = md_theme_dark_tertiary,
            onTertiary = md_theme_dark_onTertiary,
            tertiaryContainer = md_theme_dark_tertiaryContainer,
            onTertiaryContainer = md_theme_dark_onTertiaryContainer,
            error = md_theme_dark_error,
            errorContainer = md_theme_dark_errorContainer,
            onError = md_theme_dark_onError,
            onErrorContainer = md_theme_dark_onErrorContainer,
            background = md_theme_dark_background,
            onBackground = md_theme_dark_onBackground,
            surface = md_theme_dark_surface,
            onSurface = md_theme_dark_onSurface,
            surfaceVariant = md_theme_dark_surfaceVariant,
            onSurfaceVariant = md_theme_dark_onSurfaceVariant,
            outline = md_theme_dark_outline,
            inverseOnSurface = md_theme_dark_inverseOnSurface,
            inverseSurface = md_theme_dark_inverseSurface,
        )
    } else {
        lightColorScheme(
            primary = md_theme_light_primary,
            onPrimary = md_theme_light_onPrimary,
            primaryContainer = md_theme_light_primaryContainer,
            onPrimaryContainer = md_theme_light_onPrimaryContainer,
            secondary = md_theme_light_secondary,
            onSecondary = md_theme_light_onSecondary,
            secondaryContainer = md_theme_light_secondaryContainer,
            onSecondaryContainer = md_theme_light_onSecondaryContainer,
            tertiary = md_theme_light_tertiary,
            onTertiary = md_theme_light_onTertiary,
            tertiaryContainer = md_theme_light_tertiaryContainer,
            onTertiaryContainer = md_theme_light_onTertiaryContainer,
            error = md_theme_light_error,
            errorContainer = md_theme_light_errorContainer,
            onError = md_theme_light_onError,
            onErrorContainer = md_theme_light_onErrorContainer,
            background = md_theme_light_background,
            onBackground = md_theme_light_onBackground,
            surface = md_theme_light_surface,
            onSurface = md_theme_light_onSurface,
            surfaceVariant = md_theme_light_surfaceVariant,
            onSurfaceVariant = md_theme_light_onSurfaceVariant,
            outline = md_theme_light_outline,
            inverseOnSurface = md_theme_light_inverseOnSurface,
            inverseSurface = md_theme_light_inverseSurface,
        )
    }
}

@Composable
private fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme {
    val context = LocalContext.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context)
        else dynamicLightColorScheme(context)
    } else {
        getColorScheme(darkTheme)
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
        colorScheme = getDynamicColorScheme(darkTheme = darkTheme),
    ) {
        val systemBarColor = MaterialTheme.colorScheme.surface
        LaunchedEffect(systemBarColor) {
            systemUiController.setSystemBarsColor(systemBarColor)
        }
        content()
    }
}
