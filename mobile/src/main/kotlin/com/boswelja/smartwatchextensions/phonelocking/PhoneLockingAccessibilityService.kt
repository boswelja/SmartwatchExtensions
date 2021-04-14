package com.boswelja.smartwatchextensions.phonelocking

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.Observer
import com.boswelja.smartwatchextensions.common.connection.Messages.LOCK_PHONE
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.smartwatchextensions.watchmanager.item.BoolPreference
import com.boswelja.smartwatchextensions.watchmanager.item.Watch
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class PhoneLockingAccessibilityService :
    AccessibilityService(),
    MessageClient.OnMessageReceivedListener {

    private val registeredWatchObserver = Observer<List<Watch>> {
        registeredWatches = it
    }
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

    private lateinit var messageClient: MessageClient

    private var isStopping = false
    private var registeredWatches: List<Watch> = emptyList()

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            LOCK_PHONE -> {
                tryLockDevice(messageEvent)
            }
        }
    }

    override fun onServiceConnected() {
        Timber.i("onServiceConnected() called")
        messageClient = Wearable.getMessageClient(this)
        messageClient.addListener(this)
        watchManager.registeredWatches.observeForever(registeredWatchObserver)
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
     * @param messageEvent The [MessageEvent] requesting a device lock.
     */
    private fun tryLockDevice(messageEvent: MessageEvent) {
        coroutineScope.launch(Dispatchers.IO) {
            val watchId = messageEvent.sourceNodeId
            val watch = registeredWatches.firstOrNull { it.id == watchId }
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
            messageClient.removeListener(this)
            watchManager.registeredWatches.removeObserver(registeredWatchObserver)
            settingsLiveData.removeObserver(settingsObserver)
        }
    }
}
