/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.phonelocking

import android.accessibilityservice.AccessibilityService
import android.content.SharedPreferences
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.connection.Messages.LOCK_PHONE
import com.boswelja.devicemanager.common.preference.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.devicemanager.phonelocking.ui.PhoneLockingPreferenceFragment.Companion.PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE
import com.boswelja.devicemanager.phonelocking.ui.PhoneLockingPreferenceFragment.Companion.PHONE_LOCKING_MODE_KEY
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@RequiresApi(Build.VERSION_CODES.P)
class PhoneLockingAccessibilityService :
    AccessibilityService(),
    MessageClient.OnMessageReceivedListener,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val coroutineScope = MainScope()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var messageClient: MessageClient

    private val watchManager: WatchManager by lazy {
        WatchManager.getInstance(this)
    }
    private var isStopping = false
    private var lastWatch: Watch? = null

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PHONE_LOCKING_MODE_KEY &&
            sharedPreferences?.getString(key, "0") != PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE
        ) {
            Timber.i("Phone Locking mode changed, attempting to stop self")
            stopSelf()
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            LOCK_PHONE -> {
                tryLockDevice(messageEvent)
            }
        }
    }

    override fun onServiceConnected() {
        Timber.i("onServiceConnected() called")
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        messageClient = Wearable.getMessageClient(this)
        sharedPreferences.edit { putBoolean(ACCESSIBILITY_SERVICE_ENABLED_KEY, true) }
        messageClient.addListener(this)
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
            val watch = watchManager.registeredWatches.value?.firstOrNull { it.id == watchId }
            lastWatch = watch
            if (watch != null) {
                val phoneLockingEnabledForWatch =
                    watchManager.getPreference<Boolean>(watch, PHONE_LOCKING_ENABLED_KEY) == false
                if (phoneLockingEnabledForWatch) {
                    Timber.i("Trying to lock phone")
                    withContext(Dispatchers.Main) { performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN) }
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
            sharedPreferences.edit {
                putBoolean(ACCESSIBILITY_SERVICE_ENABLED_KEY, false)
                putBoolean(PHONE_LOCKING_ENABLED_KEY, false)
            }
            lastWatch?.let {
                coroutineScope.launch(Dispatchers.IO) {
                    watchManager.updatePreference(it, PHONE_LOCKING_ENABLED_KEY, false)
                }
            }
            messageClient.removeListener(this)
        }
    }

    companion object {
        const val ACCESSIBILITY_SERVICE_ENABLED_KEY = "accessibility_service_enabled"
    }
}
