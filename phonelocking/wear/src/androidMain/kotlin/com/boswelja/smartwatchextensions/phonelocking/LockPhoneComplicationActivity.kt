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
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LockPhoneComplicationActivity : ComponentActivity() {

    private val discoveryClient: DiscoveryClient by inject()
    private val messageClient: MessageClient by inject()

    private val phoneLockingStateRepository: PhoneLockingStateRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        require(intent.action == LOCK_PHONE)

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
     * Checks if phone locking is enabled, and tries to send [LOCK_PHONE] to the registered phone.
     * If phone locking is disabled, a failure animation will be shown and the activity will finish.
     */
    private suspend fun tryLockPhone() {
        val phoneLockingEnabled = phoneLockingStateRepository.getPhoneLockingState().first().phoneLockingEnabled
        if (phoneLockingEnabled) {
            sendMessage(
                LOCK_PHONE,
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
        val phone = discoveryClient.pairedPhone()
        if (phone == null) {
            showConfirmationOverlay(
                ConfirmationOverlay.FAILURE_ANIMATION,
                failMessage
            )
            return
        }

        val success = messageClient.sendMessage(
            phone.uid,
            Message(
                action,
                null
            )
        )
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
