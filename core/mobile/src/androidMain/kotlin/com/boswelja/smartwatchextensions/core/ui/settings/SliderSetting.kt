package com.boswelja.smartwatchextensions.core.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.boswelja.smartwatchextensions.core.ui.theme.DisabledAlpha

/**
 * Slider setting allows the user to pick a value from a continuous range
 * @param valueRange The possible selectable range.
 * @param value The current value.
 * @param onValueChanged Called when the user has finished adjusting the slider value.
 * @param text The text to display. This should describe the slider's purpose.
 * @param modifier [Modifier].
 * @param enabled Whether this setting is enabled. A disabled setting cannot be interacted with.
 * @param steps The number of steps the slider should have. Setting this to 0 will display a continuous slider.
 * @param summaryText The summary text to display. This should use the value passed to display the currently selected
 * value.
 */
@Composable
fun SliderSetting(
    valueRange: ClosedFloatingPointRange<Float>,
    value: Float,
    onValueChanged: (Float) -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    steps: Int = 0,
    summaryText: (@Composable (Float) -> Unit)? = null
) {
    var currentValue by remember(value) { mutableStateOf(value) }
    val textColor = if (enabled) {
        LocalContentColor.current
    } else {
        LocalContentColor.current.copy(alpha = DisabledAlpha)
    }
    Column(modifier) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.titleMedium,
            LocalContentColor provides textColor
        ) {
            text()
        }
        if (summaryText != null) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.labelLarge,
                LocalContentColor provides textColor
            ) {
                summaryText(currentValue)
            }
        }
        Slider(
            value = currentValue,
            valueRange = valueRange,
            steps = steps,
            onValueChange = { currentValue = it },
            onValueChangeFinished = {
                onValueChanged(currentValue)
            },
            enabled = enabled
        )
    }
}
