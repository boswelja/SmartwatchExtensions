package com.boswelja.devicemanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.boswelja.devicemanager.common.batterysync.References.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.devicemanager.common.connection.Messages.LOCK_PHONE
import com.boswelja.devicemanager.extensions.extensionSettingsStore
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class ActionService : JobIntentService() {

    override fun onHandleWork(intent: Intent): Unit = runBlocking {
        Timber.d("onHandleWork($intent) called")
        val phoneId = runBlocking { phoneStateStore.data.map { it.id }.first() }
        when (val action = intent.action) {
            LOCK_PHONE -> {
                val phoneLockingEnabled = extensionSettingsStore.data
                    .map { it.phoneLockingEnabled }.first()
                if (phoneLockingEnabled) {
                    sendMessage(
                        phoneId,
                        action,
                        getString(R.string.lock_phone_success),
                        getString(R.string.lock_phone_failed)
                    )
                }
            }
            REQUEST_BATTERY_UPDATE_PATH -> {
                val batterySyncEnabled = extensionSettingsStore.data
                    .map { it.batterySyncEnabled }.first()
                if (batterySyncEnabled) {
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
