package com.boswelja.devicemanager.common

import android.app.IntentService
import android.content.Intent
import android.preference.PreferenceManager

class NotificationDismissedService : IntentService("NotificationDismissedService") {

    override fun onHandleIntent(intent: Intent?) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPrefs.edit().putBoolean(PreferenceKey.BATTERY_CHARGED_NOTI_ACKNOWLEDGED, true).apply()
    }

}