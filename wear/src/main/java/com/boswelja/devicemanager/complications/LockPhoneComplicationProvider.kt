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
        setComplication(type, complicationId, complicationManager!!)
    }

    override fun onComplicationActivated(complicationId: Int, type: Int, manager: ComplicationManager?) {
        super.onComplicationActivated(complicationId, type, manager)
        setComplication(type, complicationId, manager!!)
    }

    private fun setComplication(type: Int, id: Int,  manager: ComplicationManager) {
        if (type != ComplicationData.TYPE_ICON) {
            manager.noUpdateRequired(id)
            return
        }

        val intent = Intent(this, ActionService::class.java)
        intent.putExtra(Config.INTENT_ACTION_EXTRA, Config.LOCK_PHONE_PATH)
        val pendingIntent = PendingIntent.getService(this, 101, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val complicationData = ComplicationData.Builder(type)
                .setIcon(Icon.createWithResource(this, R.drawable.ic_phonelink_lock))
                .setBurnInProtectionIcon(Icon.createWithResource(this, R.drawable.ic_phonelink_lock_darkened))
                .setTapAction(pendingIntent)
                .build()
        manager.updateComplicationData(id, complicationData)
    }
}