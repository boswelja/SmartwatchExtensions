package com.boswelja.smartwatchextensions.phonelocking

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.PHONE_LOCKING_ENABLED_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

@ExperimentalCoroutinesApi
class PhoneLockingAccessibilityService : AccessibilityService() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val watchManager: WatchManager by inject()

    private var isStopping = false

    override fun onServiceConnected() {
        Timber.i("onServiceConnected() called")
        coroutineScope.launch {
            watchManager.incomingMessages().filter { it.path == LOCK_PHONE }.collect { message ->
                tryLockDevice(message.sourceUid)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Timber.v("onAccessibilityEvent() called")
    }

    override fun onInterrupt() {
        Timber.v("onInterrupt() called")
    }

    override fun onDestroy() {
        stop()
        super.onDestroy()
    }

    /**
     * Tries to lock the device after receiving a [MessageEvent].
     * @param watchId The [Watch.uid] of the watch requesting a device lock.
     */
    private fun tryLockDevice(watchId: String) {
        coroutineScope.launch(Dispatchers.IO) {
            val watch = watchManager.getWatchById(watchId).firstOrNull()
            if (watch != null) {
                val phoneLockingEnabledForWatch = watchManager.getBoolSetting(
                    PHONE_LOCKING_ENABLED_KEY, watch
                ).first()
                if (phoneLockingEnabledForWatch) {
                    Timber.i("Trying to lock phone")
                    withContext(Dispatchers.Main) {
                        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                    }
                } else {
                    Timber.w("$watchId tried to lock phone, doesn't have permission")
                }
            } else {
                Timber.w("Sending watch not registered")
                // TODO tell the watch it isn't registered
            }
        }
    }

    /** Cleans up in preparation for stopping the service. */
    private fun stop() {
        Timber.i("stop() called")
        if (!isStopping) {
            Timber.i("Stopping")
            isStopping = true
            // runBlocking here so we can update stuff without onDestroy returning
            coroutineScope.launch(Dispatchers.IO) {
                Timber.d("Disabling phone locking on all devices")
                watchManager.updatePreference(PHONE_LOCKING_ENABLED_KEY, false)
            }
        }
    }
}
