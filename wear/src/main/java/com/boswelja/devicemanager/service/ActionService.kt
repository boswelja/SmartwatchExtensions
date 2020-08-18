/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.service

import android.app.IntentService
import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.References.LOCK_PHONE_PATH
import com.boswelja.devicemanager.common.batterysync.References.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_ID_KEY
import com.boswelja.devicemanager.ConfirmationActivityHandler
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

class ActionService : IntentService("ActionService") {

  private lateinit var messageClient: MessageClient
  private lateinit var sharedPreferences: SharedPreferences
  private lateinit var phoneId: String

  override fun onCreate() {
    super.onCreate()
    messageClient = Wearable.getMessageClient(this)
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    phoneId = sharedPreferences.getString(PHONE_ID_KEY, "") ?: ""
  }

  override fun onHandleIntent(intent: Intent?) {
    val action = intent?.getStringExtra(EXTRA_ACTION)

    if (phoneId.isNotEmpty()) {
      when (action) {
        LOCK_PHONE_PATH -> {
          if (sharedPreferences.getBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, false)) {
            sendMessage(
                phoneId,
                action,
                getString(R.string.lock_phone_success_message),
                getString(R.string.lock_phone_failed_message))
          }
        }
        REQUEST_BATTERY_UPDATE_PATH -> {
          if (sharedPreferences.getBoolean(BATTERY_SYNC_ENABLED_KEY, false)) {
            sendMessage(
                phoneId,
                action,
                getString(R.string.battery_sync_force_sync_success),
                getString(R.string.battery_sync_force_sync_fail))
          }
        }
      }
    }
  }

  private fun sendMessage(
      nodeId: String, action: String, successMessage: String, failMessage: String
  ) {
    messageClient.sendMessage(nodeId, action, null)
        .addOnSuccessListener { ConfirmationActivityHandler.successAnimation(this, successMessage) }
        .addOnFailureListener { ConfirmationActivityHandler.failAnimation(this, failMessage) }
  }

  companion object {
    const val EXTRA_ACTION = "action"
  }
}
