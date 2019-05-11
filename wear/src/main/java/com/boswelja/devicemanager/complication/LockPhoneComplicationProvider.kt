/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.complication

import android.content.Intent
import android.graphics.drawable.Icon
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationManager
import android.support.wearable.complications.ComplicationText
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.service.ActionService

class LockPhoneComplicationProvider : BaseComplicationProviderService() {

    override fun onCreateComplication(complicationId: Int, type: Int, manager: ComplicationManager?) {
        if (type != ComplicationData.TYPE_SHORT_TEXT) {
            manager?.noUpdateRequired(complicationId)
            return
        }

        val intent = Intent(this, ActionService::class.java)
        intent.putExtra(ActionService.EXTRA_ACTION, References.LOCK_PHONE_PATH)
        val pendingIntent = Compat.getForegroundService(this, intent)

        val complicationData = ComplicationData.Builder(type)
                .setIcon(Icon.createWithResource(this, R.drawable.ic_phone_lock))
                .setShortText(ComplicationText.plainText(getString(R.string.lock_phone_label)))
                .setTapAction(pendingIntent)
                .build()

        manager?.updateComplicationData(complicationId, complicationData)
    }
}