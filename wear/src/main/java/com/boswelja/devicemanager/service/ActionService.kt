/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.service

import android.app.IntentService
import android.content.ComponentName
import android.content.Intent
import android.support.wearable.complications.ProviderUpdateRequester
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.ConfirmationActivityHandler
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.References.LOCK_PHONE_PATH
import com.boswelja.devicemanager.common.batterysync.References.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.devicemanager.complication.PhoneBatteryComplicationProvider
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

class ActionService : IntentService("ActionService") {

    private lateinit var action: String

    override fun onHandleIntent(intent: Intent?) {
        action = intent!!.getStringExtra(EXTRA_ACTION)

        Utils.getCompanionNode(this)
                .addOnCompleteListener {
                    if (it.isSuccessful && !it.result?.nodes.isNullOrEmpty()) {
                        val node = it.result?.nodes?.last()
                        val prefs = PreferenceManager.getDefaultSharedPreferences(this@ActionService)
                        when (action) {
                            LOCK_PHONE_PATH -> {
                                if (prefs.getBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, false)) {
                                    sendMessage(node)
                                } else {
                                    ConfirmationActivityHandler.failAnimation(this@ActionService, getString(R.string.lock_phone_disabled_message))
                                }
                                val providerUpdateRequester = ProviderUpdateRequester(this@ActionService, ComponentName(packageName, PhoneBatteryComplicationProvider::class.java.name))
                                providerUpdateRequester.requestUpdateAll()
                            }
                            REQUEST_BATTERY_UPDATE_PATH -> {
                                if (prefs.getBoolean(BATTERY_SYNC_ENABLED_KEY, false)) {
                                    sendMessage(node)
                                } else {
                                    ConfirmationActivityHandler.failAnimation(this@ActionService, getString(R.string.battery_sync_disabled))
                                }
                            }
                        }
                    } else {
                        when (action) {
                            LOCK_PHONE_PATH -> ConfirmationActivityHandler.failAnimation(this@ActionService, getString(R.string.lock_phone_failed_message))
                            REQUEST_BATTERY_UPDATE_PATH -> ConfirmationActivityHandler.failAnimation(this@ActionService, getString(R.string.phone_battery_update_failed))
                        }
                    }
                }
    }

    private fun sendMessage(node: Node?) {
        Wearable.getMessageClient(this)
                .sendMessage(node!!.id, action, null)
                .addOnSuccessListener {
                    ConfirmationActivityHandler.successAnimation(this, getString(R.string.request_success_message))
                }
                .addOnFailureListener {
                    ConfirmationActivityHandler.failAnimation(this, getString(R.string.request_failed_message))
                }
    }

    companion object {
        const val EXTRA_ACTION = "action"
    }
}
