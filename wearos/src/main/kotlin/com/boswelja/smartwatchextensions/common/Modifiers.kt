package com.boswelja.smartwatchextensions.common

import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalView
import androidx.core.view.InputDeviceCompat
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewConfigurationCompat
import timber.log.Timber

fun Modifier.rotaryInput(
    onRotaryInput: (delta: Float) -> Unit
) = composed {
    val rootView = LocalView.current
    val context = rootView.context
    rootView.setOnGenericMotionListener { _, event ->
        Timber.d("Got pointer input")
        if (event.action == MotionEvent.ACTION_SCROLL &&
            event.isFromSource(InputDeviceCompat.SOURCE_ROTARY_ENCODER)
        ) {
            Timber.d("Handling scroll")
            val delta = -event.getAxisValue(MotionEventCompat.AXIS_SCROLL) *
                ViewConfigurationCompat.getScaledVerticalScrollFactor(
                    ViewConfiguration.get(context), context
                )
            onRotaryInput(delta)
        }
        true
    }
    rootView.isFocusable = true
    rootView.requestFocus()
    this
}
