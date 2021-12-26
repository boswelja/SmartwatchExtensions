package com.boswelja.smartwatchextensions.phonelocking

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.PHONE_LOCKING_ENABLED_KEY
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
    private val watchManager: WatchManager by inject()

    private var isStopping = false

    override fun onServiceConnected() {
        coroutineScope.launch {
            watchManager.incomingMessages().filter { it.path == LOCK_PHONE }.collect { message ->
                tryLockDevice(message.sourceUid)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) { }

    override fun onInterrupt() { }

    override fun onDestroy() {
        stop()
        super.onDestroy()
    }

    private fun tryLockDevice(watchId: String) {
        coroutineScope.launch(Dispatchers.IO) {
            val watch = watchManager.getWatchById(watchId).firstOrNull()
            if (watch != null) {
                val phoneLockingEnabledForWatch = watchManager.getBoolSetting(
                    PHONE_LOCKING_ENABLED_KEY, watch
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

    /** Cleans up in preparation for stopping the service. */
    private fun stop() {
        if (!isStopping) {
            isStopping = true
            // runBlocking here so we can update stuff without onDestroy returning
            coroutineScope.launch(Dispatchers.IO) {
                watchManager.updatePreference(PHONE_LOCKING_ENABLED_KEY, false)
            }
        }
    }
}
