package com.boswelja.smartwatchextensions.core.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals

/**
 * Creates a [SnackbarVisuals] with the given properties.
 */
fun snackbarVisuals(
    message: String,
    duration: SnackbarDuration = SnackbarDuration.Short,
    actionLabel: String? = null,
    withDismissAction: Boolean = duration == SnackbarDuration.Indefinite
) : SnackbarVisuals {
    return object : SnackbarVisuals {
        override val message: String = message
        override val actionLabel: String? = actionLabel
        override val duration: SnackbarDuration = duration
        override val withDismissAction: Boolean = withDismissAction
    }
}
