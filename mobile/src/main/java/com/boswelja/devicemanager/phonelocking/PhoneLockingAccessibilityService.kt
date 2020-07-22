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
import com.boswelja.devicemanager.common.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.devicemanager.common.References.LOCK_PHONE_PATH
import com.boswelja.devicemanager.ui.phonelocking.PhoneLockingPreferenceFragment.Companion.PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE
import com.boswelja.devicemanager.ui.phonelocking.PhoneLockingPreferenceFragment.Companion.PHONE_LOCKING_MODE_KEY
import com.boswelja.devicemanager.watchmanager.WatchManager
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

    private var watchConnectionManager: WatchManager? = null
    private var isStopping = false

    private val watchConnectionManagerConnection = object : WatchManager.Connection() {
        override fun onWatchManagerBound(watchManager: WatchManager) {
            Timber.i("onWatchManagerBound() called")
            watchConnectionManager = watchManager
        }

        override fun onWatchManagerUnbound() {
            Timber.w("onWatchManagerUnbound() called")
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PHONE_LOCKING_MODE_KEY && sharedPreferences?.getString(key, "0") != PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE) {
            Timber.i("Phone Locking mode changed, attempting to stop self")
            stopSelf()
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            LOCK_PHONE_PATH -> {
                tryLockDevice(messageEvent)
            }
        }
    }

    override fun onServiceConnected() {
        Timber.i("onServiceConnected() called")
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        messageClient = Wearable.getMessageClient(this)
        sharedPreferences.edit {
            putBoolean(ACCESSIBILITY_SERVICE_ENABLED_KEY, true)
        }
        messageClient.addListener(this)
        WatchManager.bind(this, watchConnectionManagerConnection)
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
            val phoneLockingEnabledForWatch =
                watchConnectionManager?.getBoolPrefForWatch(watchId, PHONE_LOCKING_ENABLED_KEY)
                    ?.value == true
            if (phoneLockingEnabledForWatch) {
                Timber.i("Trying to lock phone")
                withContext(Dispatchers.Main) {
                    performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                }
            } else {
                Timber.i("$watchId tried to lock phone, doesn't have permission")
            }
        }
    }

    /**
     * Cleans up in preparation for stopping the service.
     */
    private fun stop() {
        Timber.i("stop() called")
        if (!isStopping) {
            Timber.i("Stopping")
            isStopping = true
            sharedPreferences.edit {
                putBoolean(ACCESSIBILITY_SERVICE_ENABLED_KEY, false)
                putBoolean(PHONE_LOCKING_ENABLED_KEY, false)
            }
            coroutineScope.launch(Dispatchers.IO) {
                watchConnectionManager?.updatePreferenceOnWatch(PHONE_LOCKING_ENABLED_KEY)
            }
            messageClient.removeListener(this)
            unbindService(watchConnectionManagerConnection)
        }
    }

    companion object {
        const val ACCESSIBILITY_SERVICE_ENABLED_KEY = "accessibility_service_enabled"
    }
}
