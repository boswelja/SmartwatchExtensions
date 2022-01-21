package com.boswelja.smartwatchextensions.phonelocking

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.boswelja.smartwatchextensions.devicemanagement.WatchRepository
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.smartwatchextensions.settings.WatchSettingsRepository
import com.boswelja.watchconnection.core.message.MessageClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

/**
 * An accessibility service for locking the device on request.
 */
@ExperimentalCoroutinesApi
class PhoneLockingAccessibilityService : AccessibilityService() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val messageClient: MessageClient by inject()
    private val watchRepository: WatchRepository by inject()
    private val settingsRepository: WatchSettingsRepository by inject()

    override fun onServiceConnected() {
        coroutineScope.launch {
            messageClient.incomingMessages().filter { it.path == LOCK_PHONE }.collect { message ->
                tryLockDevice(message.sourceUid)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // This is irrelevant for phone locking
    }

    override fun onInterrupt() {
        // This is irrelevant for phone locking
    }

    private fun tryLockDevice(watchId: String) {
        coroutineScope.launch(Dispatchers.IO) {
            // Ensure the calling watch is registered
            val watch = watchRepository.getWatchById(watchId).firstOrNull()
            if (watch != null) {
                val phoneLockingEnabledForWatch = settingsRepository.getBoolean(
                    watch.uid, PHONE_LOCKING_ENABLED_KEY
                ).first()
                if (phoneLockingEnabledForWatch) {
                    withContext(Dispatchers.Main) {
                        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                    }
                }
            } else {
                // TODO tell the watch it isn't registered
            }
        }
    }
}
