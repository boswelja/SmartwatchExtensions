package com.boswelja.smartwatchextensions.core.ui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * Contains default values for various settings UI elements.
 */
internal object SettingDefaults {

    /**
     * The default text style used for settings title.
     */
    internal val TitleTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography.bodyLarge

    /**
     * The default text style used for settings summary.
     */
    internal val SummaryTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography.bodyMedium

    /**
     * The default text style used for settings headers.
     */
    internal val HeaderTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography.labelLarge

    /**
     * The default settings item padding.
     */
    internal val Padding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
}
