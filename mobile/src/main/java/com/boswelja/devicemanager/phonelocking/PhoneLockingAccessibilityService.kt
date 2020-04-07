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
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable

@RequiresApi(Build.VERSION_CODES.P)
class PhoneLockingAccessibilityService :
        AccessibilityService(),
        MessageClient.OnMessageReceivedListener {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var messageClient: MessageClient

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
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Do nothing
    }

    override fun onInterrupt() {
        // Do nothing
    }

    override fun onUnbind(intent: Intent?): Boolean {
        messageClient.removeListener(this)
        sharedPreferences.edit {
            putBoolean(ACCESSIBILITY_SERVICE_ENABLED_KEY, false)
            putBoolean(PHONE_LOCKING_ENABLED_KEY, false)
        }
        return super.onUnbind(intent)
    }

    companion object {
        const val ACCESSIBILITY_SERVICE_ENABLED_KEY = "accessibility_service_enabled"
    }
}