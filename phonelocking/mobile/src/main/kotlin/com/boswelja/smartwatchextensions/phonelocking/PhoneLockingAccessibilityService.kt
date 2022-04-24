package com.boswelja.smartwatchextensions.phonelocking

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import com.boswelja.smartwatchextensions.core.settings.BoolSetting
import com.boswelja.smartwatchextensions.core.settings.BoolSettingSerializer
import com.boswelja.smartwatchextensions.core.settings.UpdateBoolSetting
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.smartwatchextensions.phonelocking.PhoneLockingSettingKeys.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.core.message.MessageClient
import com.boswelja.watchconnection.serialization.MessageHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

/**
 * An accessibility service for locking the device on request.
 */
class PhoneLockingAccessibilityService : AccessibilityService() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val messageClient: MessageClient by inject()
    private val settingsRepository: WatchSettingsRepository by inject()

    private val boolMessageHandler = MessageHandler(BoolSettingSerializer, messageClient)

    override fun onServiceConnected() {
        coroutineScope.launch {
            // Start listening for lock requests
            messageClient.incomingMessages().filter { it.path == LockPhone }.collect { message ->
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

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    private fun tryLockDevice(watchId: String) {
        coroutineScope.launch {
            val phoneLockingEnabledForWatch = settingsRepository.getBoolean(watchId, PHONE_LOCKING_ENABLED_KEY).first()
            if (phoneLockingEnabledForWatch) {
                withContext(Dispatchers.Main) {
                    performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                }
            } else {
                // Try to update the watches local state
                boolMessageHandler.sendMessage(
                    watchId,
                    Message(UpdateBoolSetting, BoolSetting(PHONE_LOCKING_ENABLED_KEY, false))
                )
            }
        }
    }

    companion object {

        /**
         * Check whether this Accessibility Service is enabled.
         * @param context [Context].
         * @return true if this Accessibility Service is enabled, false otherwise.
         */
        fun isEnabled(context: Context): Boolean {
            return context.getSystemService(AccessibilityManager::class.java)
                .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
                .any { it.resolveInfo.serviceInfo.packageName == context.packageName }
        }
    }
}
