/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.complications

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationManager
import android.support.wearable.complications.ComplicationProviderService
import android.support.wearable.complications.ComplicationText
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.service.ActionService

class LockPhoneComplicationProvider : ComplicationProviderService() {

    override fun onComplicationUpdate(complicationId: Int, type: Int, complicationManager: ComplicationManager?) {
        setComplication(type, complicationId, complicationManager!!)
    }

    override fun onComplicationActivated(complicationId: Int, type: Int, manager: ComplicationManager?) {
        super.onComplicationActivated(complicationId, type, manager)
        setComplication(type, complicationId, manager!!)
    }

    private fun setComplication(type: Int, id: Int, manager: ComplicationManager) {
        if (type != ComplicationData.TYPE_SHORT_TEXT) {
            manager.noUpdateRequired(id)
            return
        }

        val intent = Intent(this, ActionService::class.java)
        intent.putExtra(ActionService.INTENT_ACTION_EXTRA, References.LOCK_PHONE_PATH)
        val pendingIntent: PendingIntent
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getService(this, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val complicationData = ComplicationData.Builder(type)
                .setIcon(Icon.createWithResource(this, R.drawable.ic_phonelink_lock))
                .setShortText(ComplicationText.plainText(getString(R.string.lock_phone_label)))
                .setTapAction(pendingIntent)
                .build()
        manager.updateComplicationData(id, complicationData)
    }
}