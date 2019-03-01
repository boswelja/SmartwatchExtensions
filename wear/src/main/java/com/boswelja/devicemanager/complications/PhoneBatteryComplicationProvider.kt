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
import android.preference.PreferenceManager
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationManager
import android.support.wearable.complications.ComplicationProviderService
import android.support.wearable.complications.ComplicationText
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.CommonUtils
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.service.ActionService

class PhoneBatteryComplicationProvider : ComplicationProviderService() {

    override fun onComplicationUpdate(complicationId: Int, type: Int, manager: ComplicationManager?) {
        manager?.updateComplicationData(complicationId, createComplication(type))
    }

    private fun createComplication(type: Int): ComplicationData {
        val intent = Intent(this, ActionService::class.java)
        intent.putExtra(References.INTENT_ACTION_EXTRA, References.REQUEST_BATTERY_UPDATE_KEY)
        val pendingIntent: PendingIntent
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getService(this, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val percent = prefs.getInt(References.BATTERY_PERCENT_KEY, -1)
        val text = if (percent > -1) String.format(getString(R.string.phone_battery_percent), percent) else getString(R.string.phone_battery_unknown_short)
        val data = ComplicationData.Builder(type)
                .setShortText(ComplicationText.plainText(text))
                .setIcon(createIcon(percent))
                .setTapAction(pendingIntent)
        if (type == ComplicationData.TYPE_RANGED_VALUE) {
            data.setMaxValue(1.0f)
                    .setMinValue(0.0f)
                    .setValue((percent.toFloat()) / 100)
        }
        return data.build()
    }

    private fun createIcon(percent: Int): Icon {
        val drawable = getDrawable(R.drawable.ic_phone_battery)!!
        drawable.level = percent
        return Icon.createWithBitmap(CommonUtils.drawableToBitmap(drawable))
    }
}