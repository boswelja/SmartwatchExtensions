package com.boswelja.devicemanager.complications

import android.graphics.drawable.Icon
import android.preference.PreferenceManager
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationManager
import android.support.wearable.complications.ComplicationProviderService
import android.support.wearable.complications.ComplicationText
import android.util.Log
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Config

class PhoneBatteryComplicationProvider : ComplicationProviderService() {

    private val tag = "PhoneBatteryComplicationProvider"

    override fun onComplicationUpdate(complicationId: Int, type: Int, manager: ComplicationManager?) {
        Log.d(tag, "onComplicationUpdate() id: " + complicationId)
        manager?.updateComplicationData(complicationId, createComplication())
        Log.d(tag, "Complication updated")
    }

    override fun onComplicationDeactivated(complicationId: Int) {
        super.onComplicationDeactivated(complicationId)
        Log.d(tag, "Complication deactivated")
    }

    private fun createComplication(): ComplicationData {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val percent = prefs.getInt(Config.BATTERY_PERCENT_KEY, 0)
        return if (percent > 0) {
            ComplicationData.Builder(ComplicationData.TYPE_RANGED_VALUE)
                    .setShortText(ComplicationText.plainText(percent.toString() + "%"))
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_smartphone_battery))
                    .setMaxValue(1.0f)
                    .setMinValue(0.0f)
                    .setValue((percent.toFloat()) / 100)
                    .build()
        } else {
            ComplicationData.Builder(ComplicationData.TYPE_RANGED_VALUE)
                    .setShortText(ComplicationText.plainText("N/A"))
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_smartphone_battery))
                    .setMinValue(0.0f)
                    .setMaxValue(1.0f)
                    .setValue(0.0f)
                    .build()
        }
    }
}