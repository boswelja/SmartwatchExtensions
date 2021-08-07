package com.boswelja.smartwatchextensions.common

import android.view.View
import androidx.wear.widget.ConfirmationOverlay
import androidx.wear.widget.ConfirmationOverlay.DEFAULT_ANIMATION_DURATION_MS
import kotlinx.coroutines.channels.Channel

/**
 * Shows a [ConfirmationOverlay] above [this.rootView]. This function will block until the animation
 * has ended.
 * @param type The [ConfirmationOverlay] type. See [ConfirmationOverlay.setType].
 * @param message The message to be shown in the overlay. See [ConfirmationOverlay.setMessage].
 * @param duration The duration of the animation. See [ConfirmationOverlay.setDuration].
 */
suspend fun View.showConfirmationOverlay(
    type: Int,
    message: CharSequence,
    duration: Int = DEFAULT_ANIMATION_DURATION_MS
) {
    val channel = Channel<Boolean>()
    ConfirmationOverlay()
        .setDuration(duration)
        .setType(type)
        .setMessage(message)
        .setOnAnimationFinishedListener { channel.trySend(true) }
        .showAbove(rootView)

    // Block until animation finishes
    channel.receive()
}
