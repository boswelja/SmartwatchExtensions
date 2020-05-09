/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
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
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.Extensions.toByteArray
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References.CAPABILITY_WATCH_APP
import com.boswelja.devicemanager.common.References.LOCK_PHONE_PATH
import com.boswelja.devicemanager.common.References.REQUEST_LAUNCH_APP_PATH
import com.boswelja.devicemanager.common.batterysync.References.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.devicemanager.common.batterysync.Utils
import com.boswelja.devicemanager.common.dndsync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.devicemanager.common.setup.References.CHECK_WATCH_REGISTERED_PATH
import com.boswelja.devicemanager.common.setup.References.WATCH_NOT_REGISTERED_PATH
import com.boswelja.devicemanager.common.setup.References.WATCH_REGISTERED_PATH
import com.boswelja.devicemanager.phonelocking.Utils.isDeviceAdminEnabled
import com.boswelja.devicemanager.ui.base.BaseWatchPickerPreferenceActivity.Companion.EXTRA_PREFERENCE_KEY
import com.boswelja.devicemanager.ui.batterysync.BatterySyncPreferenceActivity
import com.boswelja.devicemanager.ui.dndsync.DnDSyncPreferenceActivity
import com.boswelja.devicemanager.ui.main.MainActivity
import com.boswelja.devicemanager.ui.phonelocking.PhoneLockingPreferenceFragment.Companion.PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE
import com.boswelja.devicemanager.ui.phonelocking.PhoneLockingPreferenceFragment.Companion.PHONE_LOCKING_MODE_KEY
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchMessageReceiver : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        Timber.i("onMessageReceived() called")
        when (messageEvent?.path) {
            LOCK_PHONE_PATH -> tryLockDevice()
            REQUEST_LAUNCH_APP_PATH -> {
                val key = String(messageEvent.data, Charsets.UTF_8)
                launchAppTo(key)
            }
            REQUEST_BATTERY_UPDATE_PATH ->
                Utils.updateBatteryStats(this, CAPABILITY_WATCH_APP)
            REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH ->
                sendInterruptFilterAccess(messageEvent.sourceNodeId!!)
            CHECK_WATCH_REGISTERED_PATH -> sendIsWatchRegistered(messageEvent.sourceNodeId!!)
        }
    }

    /**
     * Tries to lock the device via Device Administrator permissions.
     */
    private fun tryLockDevice() {
        Timber.i("tryLockDevice() called")
        val isInDeviceAdminMode = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(PHONE_LOCKING_MODE_KEY, "0") != PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE
        val isDeviceAdminEnabled = isDeviceAdminEnabled(this)
        if (isInDeviceAdminMode && isDeviceAdminEnabled) {
            Timber.i("Trying to lock device")
            val devicePolicyManager: DevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            devicePolicyManager.lockNow()
        }
    }

    /**
     * Launches Wearable Extensions to an activity containing a specified preference key.
     * @param key The key to launch Wearable Extensions to.
     */
    private fun launchAppTo(key: String?) {
        Timber.i("launchAppTo($key) called")
        when (key) {
            PreferenceKey.PHONE_LOCKING_ENABLED_KEY -> {
                Intent(this, MainActivity::class.java).apply {
                    putExtra(EXTRA_PREFERENCE_KEY, key)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.also {
                    startActivity(it)
                }
            }
            PreferenceKey.BATTERY_SYNC_ENABLED_KEY,
            PreferenceKey.BATTERY_SYNC_INTERVAL_KEY,
            PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY,
            PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY
            -> {
                Intent(this, BatterySyncPreferenceActivity::class.java).apply {
                    putExtra(EXTRA_PREFERENCE_KEY, key)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.also {
                    startActivity(it)
                }
            }
            PreferenceKey.DND_SYNC_TO_WATCH_KEY,
            PreferenceKey.DND_SYNC_TO_PHONE_KEY,
            PreferenceKey.DND_SYNC_WITH_THEATER_KEY
            -> {
                Intent(this, DnDSyncPreferenceActivity::class.java).apply {
                    putExtra(EXTRA_PREFERENCE_KEY, key)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.also {
                    startActivity(it)
                }
            }
            else -> {
                Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.also {
                    startActivity(it)
                }
            }
        }
    }

    /**
     * Tells the source node whether we are allowed to set the state of Do not Disturb.
     * @param sourceNodeId The target node ID to send the response to.
     */
    private fun sendInterruptFilterAccess(sourceNodeId: String) {
        Timber.i("sendInterruptFilterAccess() called")
        val hasDnDAccess = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notiManager.isNotificationPolicyAccessGranted
        } else {
            true
        }
        Wearable.getMessageClient(this).sendMessage(
                sourceNodeId,
                REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH,
                hasDnDAccess.toByteArray())
    }

    /**
     * Tells the source node whether it is registered with Wearable Extensions.
     * @param sourceNodeId The target node ID to send the response to.
     */
    private fun sendIsWatchRegistered(sourceNodeId: String) {
        Timber.i("sendIsWatchRegistered() called")
        MainScope().launch(Dispatchers.IO) {
            // TODO Access the database through WatchConnectionService
            val database = WatchDatabase.open(this@WatchMessageReceiver)
            val messageCode = if (database.watchDao().findById(sourceNodeId) != null) {
                WATCH_REGISTERED_PATH
            } else {
                WATCH_NOT_REGISTERED_PATH
            }
            Wearable.getMessageClient(this@WatchMessageReceiver).sendMessage(sourceNodeId, messageCode, null)
            database.close()
        }
    }
}
