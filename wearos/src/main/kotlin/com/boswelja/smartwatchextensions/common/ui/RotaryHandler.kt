package com.boswelja.smartwatchextensions.common.ui

import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.core.view.InputDeviceCompat
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewConfigurationCompat

/**
 * An effect for handling rotary input from the device.
 * Calling this in your composable sets a [View.OnGenericMotionListener] on [LocalView].
 * @param onRotaryInput The action invoked when rotary input is received.
 */
@Composable
fun RotaryHandler(onRotaryInput: (scrollDelta: Float) -> Unit) {
    val rootView = LocalView.current

    // Handle onRotaryInput changes
    val currentOnRotaryInput by rememberUpdatedState(onRotaryInput)

    // Calculate verticalScrollScale
    val viewConfig = remember {
        ViewConfiguration.get(rootView.context)
    }
    val verticalScrollScale = remember {
        ViewConfigurationCompat.getScaledVerticalScrollFactor(
            viewConfig, rootView.context
        )
    }

    val rotaryInputCallback = remember {
        View.OnGenericMotionListener { _, event ->
            if (event.action == MotionEvent.ACTION_SCROLL &&
                event.isFromSource(InputDeviceCompat.SOURCE_ROTARY_ENCODER)
            ) {
                // Calculate the scroll delta
                val delta = -event.getAxisValue(MotionEventCompat.AXIS_SCROLL) * verticalScrollScale
                currentOnRotaryInput(delta)
            }
            true
        }
    }

    // Bind to LifecycleOwner so we only set callback when needed
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        rootView.setOnGenericMotionListener(rotaryInputCallback)

        // Request focus so we get rotary events
        rootView.isFocusable = true
        rootView.requestFocus()
    }
}
