package com.boswelja.smartwatchextensions.watchmanager.connection.wearos

import android.content.Intent
import com.boswelja.smartwatchextensions.batterysync.Utils.updateBatteryStats
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySyncSettingsActivity
import com.boswelja.smartwatchextensions.common.Compat
import com.boswelja.smartwatchextensions.common.batterysync.References.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.smartwatchextensions.common.connection.Messages.CHECK_WATCH_REGISTERED_PATH
import com.boswelja.smartwatchextensions.common.connection.Messages.LAUNCH_APP
import com.boswelja.smartwatchextensions.common.connection.Messages.WATCH_NOT_REGISTERED_PATH
import com.boswelja.smartwatchextensions.common.connection.Messages.WATCH_REGISTERED_PATH
import com.boswelja.smartwatchextensions.common.dndsync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.smartwatchextensions.common.toByteArray
import com.boswelja.smartwatchextensions.dndsync.ui.DnDSyncSettingsActivity
import com.boswelja.smartwatchextensions.main.MainActivity
import com.boswelja.smartwatchextensions.watchmanager.database.WatchDatabase
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchMessageReceiver : WearableListenerService() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        Timber.i("onMessageReceived() called")
        when (messageEvent?.path) {
            LAUNCH_APP -> {
                val key = String(messageEvent.data, Charsets.UTF_8)
                launchAppTo(key)
            }
            REQUEST_BATTERY_UPDATE_PATH ->
                coroutineScope.launch { updateBatteryStats(this@WatchMessageReceiver) }
            REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH ->
                sendInterruptFilterAccess(messageEvent.sourceNodeId!!)
            CHECK_WATCH_REGISTERED_PATH -> sendIsWatchRegistered(messageEvent.sourceNodeId!!)
        }
    }

    /**
     * Launches Smartwatch Extensions to an activity containing a specified preference key.
     * @param key The key to launch Smartwatch Extensions to.
     */
    private fun launchAppTo(key: String?) {
        Timber.i("launchAppTo($key) called")
        when (key) {
            PHONE_LOCKING_ENABLED_KEY -> {
                Intent(this, MainActivity::class.java)
                    .apply {
                        putExtra(EXTRA_PREFERENCE_KEY, key)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    .also { startActivity(it) }
            }
            PreferenceKey.BATTERY_SYNC_ENABLED_KEY,
            PreferenceKey.BATTERY_SYNC_INTERVAL_KEY,
            PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY,
            PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY -> {
                Intent(this, BatterySyncSettingsActivity::class.java)
                    .apply {
                        putExtra(EXTRA_PREFERENCE_KEY, key)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    .also { startActivity(it) }
            }
            PreferenceKey.DND_SYNC_TO_WATCH_KEY,
            PreferenceKey.DND_SYNC_TO_PHONE_KEY,
            PreferenceKey.DND_SYNC_WITH_THEATER_KEY -> {
                Intent(this, DnDSyncSettingsActivity::class.java)
                    .apply {
                        putExtra(EXTRA_PREFERENCE_KEY, key)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    .also { startActivity(it) }
            }
            else -> {
                Intent(this, MainActivity::class.java)
                    .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    .also { startActivity(it) }
            }
        }
    }

    /**
     * Tells the source node whether we are allowed to set the state of Do not Disturb.
     * @param sourceNodeId The target node ID to send the response to.
     */
    private fun sendInterruptFilterAccess(sourceNodeId: String) {
        Timber.i("sendInterruptFilterAccess() called")
        val hasDnDAccess = Compat.canSetDnD(this)
        Wearable.getMessageClient(this)
            .sendMessage(
                sourceNodeId,
                REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH,
                hasDnDAccess.toByteArray()
            )
    }

    /**
     * Tells the source node whether it is registered with Smartwatch Extensions.
     * @param sourceNodeId The target node ID to send the response to.
     */
    private fun sendIsWatchRegistered(sourceNodeId: String) {
        Timber.i("sendIsWatchRegistered() called")
        MainScope().launch(Dispatchers.IO) {
            // TODO Access the database through WatchConnectionService
            val database = WatchDatabase.getInstance(this@WatchMessageReceiver)
            val messageCode =
                if (database.getById(sourceNodeId) != null) {
                    WATCH_REGISTERED_PATH
                } else {
                    WATCH_NOT_REGISTERED_PATH
                }
            Wearable.getMessageClient(this@WatchMessageReceiver)
                .sendMessage(sourceNodeId, messageCode, null)
            database.close()
        }
    }

    companion object {
        const val EXTRA_PREFERENCE_KEY = "extra_preference_key"
    }
}
