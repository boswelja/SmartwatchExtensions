package com.boswelja.devicemanager.receiver

import android.content.Context
import android.content.Intent
import com.boswelja.devicemanager.common.BaseBootReceiver
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.service.InterruptFilterLocalChangeListener

class BootReceiver : BaseBootReceiver() {

    override fun isInterruptFilterSyncSending(): Boolean {
        return sharedPreferences.getBoolean(INTERRUPT_FILTER_SYNC_TO_PHONE_KEY, false)
    }

    override fun startInterruptFilterSyncService(context: Context?) {
        val intent = Intent(context, InterruptFilterLocalChangeListener::class.java)
        Compat.startForegroundService(context!!, intent)
    }
}