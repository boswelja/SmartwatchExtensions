package com.boswelja.devicemanager.phonelocking

import android.accessibilityservice.AccessibilityService
import android.content.Intent
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
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.P)
class PhoneLockingAccessibilityService :
        AccessibilityService(),
        MessageClient.OnMessageReceivedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private val coroutineScope = MainScope()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var messageClient: MessageClient

    private var watchConnectionManager: WatchConnectionService? = null
    private var isStopping = false

    private val watchConnectionManagerConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            watchConnectionManager = service
        }

        override fun onWatchManagerUnbound() {}
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PHONE_LOCKING_MODE_KEY && sharedPreferences?.getString(key, "0") != PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE) {
            stopSelf()
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            LOCK_PHONE_PATH -> {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            }
        }
    }

    override fun onServiceConnected() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        messageClient = Wearable.getMessageClient(this)
        sharedPreferences.edit {
            putBoolean(ACCESSIBILITY_SERVICE_ENABLED_KEY, true)
        }
        messageClient.addListener(this)
        WatchConnectionService.bind(this, watchConnectionManagerConnection)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Do nothing
    }

    override fun onInterrupt() {
        // Do nothing
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stop()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        stop()
        super.onDestroy()
    }

    private fun stop() {
        if (!isStopping) {
            isStopping = true
            sharedPreferences.edit {
                putBoolean(ACCESSIBILITY_SERVICE_ENABLED_KEY, false)
                putBoolean(PHONE_LOCKING_ENABLED_KEY, false)
            }
            coroutineScope.launch {
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