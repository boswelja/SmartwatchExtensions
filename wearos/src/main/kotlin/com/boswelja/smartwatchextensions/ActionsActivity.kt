package com.boswelja.smartwatchextensions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.widget.ConfirmationOverlay
import com.boswelja.smartwatchextensions.common.batterysync.References.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.smartwatchextensions.common.connection.Messages.LOCK_PHONE
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * An Activity for handling Actions triggered when the app is not running, e.g. from Complications,
 * Tiles etc.
 */
class ActionsActivity : ComponentActivity() {

    private val messageClient by lazy { Wearable.getMessageClient(this) }

    private var isLoading by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        Text(
                            text = stringResource(R.string.loading),
                            style = MaterialTheme.typography.display3
                        )
                    }
                }
            }
        }

        lifecycleScope.launch {
            handleAction()
        }
    }

    /**
     * Checks the action for the intent this activity was started with, and calls the appropriate
     * function.
     */
    private suspend fun handleAction() {
        when (intent.action) {
            LOCK_PHONE -> {
                Timber.d("Trying to lock phone")
                tryLockPhone()
            }
            REQUEST_BATTERY_UPDATE_PATH -> {
                Timber.d("Trying to update battery stats")
                tryUpdateBatteryStats()
            }
            else -> {
                Timber.w("Invalid action code received")
                finish()
            }
        }
    }

    /**
     * Checks if phone locking is enabled, and tries to send [LOCK_PHONE] to the registered phone.
     * If phone locking is disabled, a failure animation will be shown and the activity will finish.
     */
    private suspend fun tryLockPhone() {
        val phoneLockingEnabled = extensionSettingsStore.data
            .map { it.phoneLockingEnabled }.first()
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
     * Checks if battery sync is enabled, and tries to send [REQUEST_BATTERY_UPDATE_PATH] to the
     * registered phone. If battery sync is disabled, a failure animation will be shown and the
     * activity will finish.
     */
    private suspend fun tryUpdateBatteryStats() {
        val batterySyncEnabled = extensionSettingsStore.data
            .map { it.batterySyncEnabled }.first()
        if (batterySyncEnabled) {
            sendMessage(
                REQUEST_BATTERY_UPDATE_PATH,
                getString(R.string.battery_sync_refresh_success),
                getString(R.string.battery_sync_refresh_failed)
            )
        } else {
            showConfirmationOverlay(
                ConfirmationOverlay.FAILURE_ANIMATION,
                getString(R.string.battery_sync_disabled)
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
        val nodeId = getPhoneID()
        try {
            messageClient.sendMessage(nodeId, action, null).await()
            // If we get this far, we've succeeded
            showConfirmationOverlay(
                ConfirmationOverlay.SUCCESS_ANIMATION,
                successMessage
            )
        } catch (e: Exception) {
            // Message send failed
            showConfirmationOverlay(
                ConfirmationOverlay.FAILURE_ANIMATION,
                failMessage
            )
        }
    }

    /**
     * Gets the registered phone ID.
     */
    private suspend fun getPhoneID(): String {
        return phoneStateStore.data.map { it.id }.first()
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
        isLoading = false
        ConfirmationOverlay()
            .setOnAnimationFinishedListener { finish() }
            .setType(type)
            .setMessage(message)
            .showOn(this)
    }
}
