package com.boswelja.devicemanager.complications

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationManager
import android.support.wearable.complications.ComplicationProviderService
import android.util.Log
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Config

class LockPhoneComplicationProvider : ComplicationProviderService() {

    private val tag = "LockPhoneComplicationProvider"

    override fun onComplicationUpdate(complicationId: Int, type: Int, complicationManager: ComplicationManager?) {
        Log.d(tag, "onComplicationUpdate() id: " + complicationId)

    }

    override fun onComplicationActivated(complicationId: Int, type: Int, manager: ComplicationManager?) {
        super.onComplicationActivated(complicationId, type, manager)
        Log.d(tag, "Complication activated")

        if (type != ComplicationData.TYPE_ICON) {
            manager?.noUpdateRequired(complicationId)
            return
        }

        val intent = Intent()
        intent.action = Config.INTENT_PERFORM_ACTION
        intent.putExtra("action", Config.TYPE_LOCK_PHONE)
        val pendingIntent = PendingIntent.getBroadcast(this, 101, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val complicationData = ComplicationData.Builder(type)
                .setIcon(Icon.createWithResource(this, R.drawable.ic_phonelink_lock))
                .setTapAction(pendingIntent)
                .build()
        manager?.updateComplicationData(complicationId, complicationData)
    }

    override fun onComplicationDeactivated(complicationId: Int) {
        super.onComplicationDeactivated(complicationId)
        Log.d(tag, "Complication deactivated")
    }
}