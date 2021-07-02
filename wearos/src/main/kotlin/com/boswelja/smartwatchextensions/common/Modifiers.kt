package com.boswelja.smartwatchextensions.common

import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalView
import androidx.core.view.InputDeviceCompat
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewConfigurationCompat

fun Modifier.rotaryInput(
    onRotaryInput: (delta: Float) -> Unit
) = composed {
    val rootView = LocalView.current
    val viewConfig = ViewConfiguration.get(rootView.context)
    val verticalScrollScale = ViewConfigurationCompat.getScaledVerticalScrollFactor(
        viewConfig, rootView.context
    )

    rootView.setOnGenericMotionListener { _, event ->
        if (event.action == MotionEvent.ACTION_SCROLL &&
            event.isFromSource(InputDeviceCompat.SOURCE_ROTARY_ENCODER)
        ) {
            val delta = -event.getAxisValue(MotionEventCompat.AXIS_SCROLL) * verticalScrollScale
            onRotaryInput(delta)
        }
        true
    }

    rootView.isFocusable = true
    rootView.requestFocus()
    this
}
