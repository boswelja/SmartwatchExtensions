package com.boswelja.devicemanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.batterysync.References.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.devicemanager.common.connection.Messages.LOCK_PHONE
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_ID_KEY
import com.google.android.gms.wearable.Wearable
import timber.log.Timber

class ActionService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        Timber.d("onHandleWork($intent) called")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.getString(PHONE_ID_KEY, "")?.let { phoneId ->
            when (val action = intent.action) {
                LOCK_PHONE -> {
                    if (sharedPreferences.getBoolean(PHONE_LOCKING_ENABLED_KEY, false)) {
                        sendMessage(
                            phoneId,
                            action,
                            getString(R.string.lock_phone_success),
                            getString(R.string.lock_phone_failed)
                        )
                    }
                }
                REQUEST_BATTERY_UPDATE_PATH -> {
                    if (sharedPreferences.getBoolean(BATTERY_SYNC_ENABLED_KEY, false)) {
                        sendMessage(
                            phoneId,
                            action,
                            getString(R.string.battery_sync_refresh_success),
                            getString(R.string.battery_sync_refresh_failed)
                        )
                    }
                }
            }
        }
    }

    private fun sendMessage(
        nodeId: String,
        action: String,
        successMessage: String,
        failMessage: String
    ) {
        Wearable.getMessageClient(this)
            .sendMessage(nodeId, action, null)
            .addOnSuccessListener {
                ConfirmationActivityHandler.successAnimation(this, successMessage)
            }
            .addOnFailureListener { ConfirmationActivityHandler.failAnimation(this, failMessage) }
    }

    companion object {
        private const val workId = 271738

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, ActionService::class.java, workId, intent)
        }
    }
}

class ActionServiceStarter : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.i("Got broadcast, enqueueing work")
        intent?.setClass(context!!, ActionService::class.java)
        ActionService.enqueueWork(context!!, intent!!)
    }
}
