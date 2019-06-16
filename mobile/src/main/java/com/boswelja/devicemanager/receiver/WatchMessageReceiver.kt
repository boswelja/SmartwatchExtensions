/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.boswelja.devicemanager.common.Extensions.toByteArray
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References.CAPABILITY_WATCH_APP
import com.boswelja.devicemanager.common.References.LOCK_PHONE_PATH
import com.boswelja.devicemanager.common.References.REQUEST_LAUNCH_APP_PATH
import com.boswelja.devicemanager.common.batterysync.References.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.devicemanager.common.batterysync.Utils
import com.boswelja.devicemanager.common.interruptfiltersync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity.Companion.EXTRA_PREFERENCE_KEY
import com.boswelja.devicemanager.ui.batterysync.BatterySyncPreferenceActivity
import com.boswelja.devicemanager.ui.interruptfiltersync.InterruptFilterSyncPreferenceActivity
import com.boswelja.devicemanager.ui.main.MainActivity
import com.boswelja.devicemanager.ui.main.ExtensionsFragment
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

class WatchMessageReceiver : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        when (messageEvent?.path) {
            LOCK_PHONE_PATH -> {
                val devicePolicyManager: DevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                if (devicePolicyManager.isAdminActive(DeviceAdminChangeReceiver().getWho(this))) {
                    devicePolicyManager.lockNow()
                }
            }
            REQUEST_LAUNCH_APP_PATH -> {
                val key = String(messageEvent.data, Charsets.UTF_8)
                when (key) {
                    PreferenceKey.PHONE_LOCKING_ENABLED_KEY -> {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra(EXTRA_PREFERENCE_KEY, key)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    }
                    PreferenceKey.BATTERY_SYNC_ENABLED_KEY,
                    PreferenceKey.BATTERY_SYNC_INTERVAL_KEY,
                    PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY,
                    PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY -> {
                        val intent = Intent(this, BatterySyncPreferenceActivity::class.java).apply {
                            putExtra(EXTRA_PREFERENCE_KEY, key)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    }
                    PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY,
                    PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY,
                    PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY -> {
                        val intent = Intent(this, InterruptFilterSyncPreferenceActivity::class.java).apply {
                            putExtra(EXTRA_PREFERENCE_KEY, key)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    }
                }
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(EXTRA_PREFERENCE_KEY, key)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            REQUEST_BATTERY_UPDATE_PATH ->
                Utils.updateBatteryStats(this, CAPABILITY_WATCH_APP)
            REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH -> {
                val hasDnDAccess = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val notiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notiManager.isNotificationPolicyAccessGranted
                } else {
                    true
                }
                Wearable.getMessageClient(this)
                        .sendMessage(
                                messageEvent.sourceNodeId!!,
                                REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH,
                                hasDnDAccess.toByteArray())
            }
        }
    }
}
