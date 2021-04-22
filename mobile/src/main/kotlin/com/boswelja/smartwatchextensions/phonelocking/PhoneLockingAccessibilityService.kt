package com.boswelja.smartwatchextensions.phonelocking

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.Observer
import com.boswelja.smartwatchextensions.common.connection.Messages.LOCK_PHONE
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.smartwatchextensions.watchmanager.item.BoolPreference
import com.boswelja.watchconnection.core.MessageListener
import com.boswelja.watchconnection.core.Watch
import com.google.android.gms.wearable.MessageEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID

class PhoneLockingAccessibilityService :
    AccessibilityService(),
    MessageListener {

    private val settingsObserver = Observer<Array<BoolPreference>> { prefs ->
        if (prefs.none { it.value }) stop()
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val watchManager: WatchManager by lazy {
        WatchManager.getInstance(this)
    }

    private val settingsLiveData by lazy {
        watchManager.settingsDatabase.boolPrefDao().getAllObservableForKey(
            PHONE_LOCKING_ENABLED_KEY
        )
    }

    private var isStopping = false

    override fun onMessageReceived(sourceWatchId: UUID, message: String, data: ByteArray?) {
        when (message) {
            LOCK_PHONE -> {
                tryLockDevice(sourceWatchId)
            }
        }
    }

    override fun onServiceConnected() {
        Timber.i("onServiceConnected() called")
        watchManager.registerMessageListener(this)
        settingsLiveData.observeForever(settingsObserver)
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
     * @param watchId The [Watch.id] of the watch requesting a device lock.
     */
    private fun tryLockDevice(watchId: UUID) {
        coroutineScope.launch(Dispatchers.IO) {
            val watch = watchManager.getWatchById(watchId)
            if (watch != null) {
                val phoneLockingEnabledForWatch = watchManager.getPreference<Boolean>(
                    watch.id, PHONE_LOCKING_ENABLED_KEY
                ) == false
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
            }
        }
    }

    /** Cleans up in preparation for stopping the service. */
    private fun stop() {
        Timber.i("stop() called")
        if (!isStopping) {
            Timber.i("Stopping")
            isStopping = true
            coroutineScope.launch(Dispatchers.IO) {
                watchManager.settingsDatabase.boolPrefDao().updateAllForKey(
                    PHONE_LOCKING_ENABLED_KEY, false
                )
            }
            watchManager.unregisterMessageListener(this)
            settingsLiveData.removeObserver(settingsObserver)
        }
    }
}
