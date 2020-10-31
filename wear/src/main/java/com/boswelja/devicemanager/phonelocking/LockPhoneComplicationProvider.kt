/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.phonelocking

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationManager
import android.support.wearable.complications.ComplicationText
import com.boswelja.devicemanager.ActionServiceStarter
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.BaseComplicationProviderService
import com.boswelja.devicemanager.common.References.LOCK_PHONE_PATH

class LockPhoneComplicationProvider : BaseComplicationProviderService() {

    override fun onCreateComplication(
        complicationId: Int,
        type: Int,
        manager: ComplicationManager?
    ) {
        if (type != ComplicationData.TYPE_SHORT_TEXT) {
            manager?.noUpdateRequired(complicationId)
            return
        }

        val intent =
            Intent(this, ActionServiceStarter::class.java).apply { action = LOCK_PHONE_PATH }
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val complicationData =
            ComplicationData.Builder(type)
                .setIcon(Icon.createWithResource(this, R.drawable.ic_phone_lock))
                .setShortText(ComplicationText.plainText(getString(R.string.lock_phone_label)))
                .setTapAction(pendingIntent)
                .build()

        manager?.updateComplicationData(complicationId, complicationData)
    }
}
