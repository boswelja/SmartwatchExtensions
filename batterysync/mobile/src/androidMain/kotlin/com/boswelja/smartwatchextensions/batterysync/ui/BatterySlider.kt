package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.batterysync.R
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySliderDefaults.PROGRESS_FACTOR
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySliderDefaults.STEP_SIZE
import kotlin.math.roundToInt

/**
 * A setting containing some text and a slider. This allows the user to pick a battery percent value.
 * @param valueRange The possible selectable range. Start and end values should be set in
 * [BatterySliderDefaults.STEP_SIZE] increments. The start value should be at or above 0f and the end value should be at
 * or below 1f.
 * @param value The current value.
 * @param onValueChanged Called when the user has finished adjusting the slider value.
 * @param text The text to display. This should describe the slider's purpose.
 * @param modifier [Modifier].
 */
@Composable
fun BatterySliderSetting(
    valueRange: ClosedFloatingPointRange<Float>,
    value: Float,
    onValueChanged: (Float) -> Unit,
    text: @Composable () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var currentValue by remember(value) { mutableStateOf(value) }
    val steps = remember(valueRange) {
        ((valueRange.endInclusive - valueRange.start) / STEP_SIZE).roundToInt() - 1
    }
    Column(modifier) {
        text()
        Text(stringResource(R.string.battery_percent, (currentValue * PROGRESS_FACTOR).roundToInt()))
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

/**
 * Contains default values for Battery Sliders.
 */
object BatterySliderDefaults {

    /**
     * The factor used to convert between integer and floating point percentages.
     */
    const val PROGRESS_FACTOR = 100f

    /**
     * The default step size for battery sliders.
     */
    const val STEP_SIZE = 0.05f
}
