package com.boswelja.smartwatchextensions.phonelocking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.widget.ConfirmationOverlay
import com.boswelja.smartwatchextensions.core.devicemanagement.phoneStateStore
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.android.ext.android.inject

/**
 * A basic activity to handle making a lock phone request. An activity is used to provide more consistent behavior
 * compared to a broadcast receiver. This should only be used when the pp is not already in the foreground, for example
 * as a complication click action.
 */
class LockPhoneComplicationActivity : ComponentActivity() {

    private val phoneLockingStateRepository: PhoneLockingStateRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        require(intent.action == LockPhone)

        setContent {
            MaterialTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Please Wait",
                        style = MaterialTheme.typography.display3
                    )
                }
            }
        }

        lifecycleScope.launch {
            tryLockPhone()
        }
    }
    /**
     * Checks if phone locking is enabled, and tries to send [LockPhone] to the registered phone.
     * If phone locking is disabled, a failure animation will be shown and the activity will finish.
     */
    private suspend fun tryLockPhone() {
        val phoneLockingEnabled = phoneLockingStateRepository.getPhoneLockingState().first().phoneLockingEnabled
        if (phoneLockingEnabled) {
            sendMessage(
                LockPhone,
                getString(R.string.lock_phone_success),
                getString(R.string.lock_phone_failed)
            )
        } else {
            showConfirmationOverlay(
                ConfirmationOverlay.FAILURE_ANIMATION,
                getString(R.string.lock_phone_disabled)
            )
        }
    }

    /**
     * Send a message to the registered phone.
     * @param action The message path to send to the phone.
     * @param successMessage The message to display when sending succeeds.
     * @param failMessage The message to display when sending fails.
     */
    private suspend fun sendMessage(
        action: String,
        successMessage: CharSequence,
        failMessage: CharSequence
    ) {
        val messageClient = Wearable.getMessageClient(this)

        val targetId = phoneStateStore.data.map { it.id }.first()

        val success = try {
            messageClient.sendMessage(
                targetId,
                action,
                null
            ).await()
            true
        } catch (_: ApiException) {
            false
        }

        if (success) {
            showConfirmationOverlay(
                ConfirmationOverlay.SUCCESS_ANIMATION,
                successMessage
            )
        } else {
            showConfirmationOverlay(
                ConfirmationOverlay.FAILURE_ANIMATION,
                failMessage
            )
        }
    }

    /**
     * Shows a [ConfirmationOverlay]. The activity will finish when the animation ends.
     * @param type The animation type to show in the overlay. See [ConfirmationOverlay.setType].
     * @param message The message to show with the animation. See [ConfirmationOverlay.setMessage].
     */
    private fun showConfirmationOverlay(
        type: Int,
        message: CharSequence
    ) {
        ConfirmationOverlay()
            .setOnAnimationFinishedListener { finish() }
            .setType(type)
            .setMessage(message)
            .showOn(this)
    }
}
