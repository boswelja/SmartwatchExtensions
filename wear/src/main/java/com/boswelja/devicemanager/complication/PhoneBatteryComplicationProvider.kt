/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.complication

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationManager
import android.support.wearable.complications.ComplicationText
import android.support.wearable.complications.ProviderUpdateRequester
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.batterysync.References.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.devicemanager.service.ActionService

class PhoneBatteryComplicationProvider : BaseComplicationProviderService() {

  override fun onCreateComplication(complicationId: Int, type: Int, manager: ComplicationManager?) {
    if (type != ComplicationData.TYPE_SHORT_TEXT && type != ComplicationData.TYPE_RANGED_VALUE) {
      manager?.noUpdateRequired(complicationId)
    }

    val intent = Intent(this, ActionService::class.java).apply {
      putExtra(ActionService.EXTRA_ACTION, REQUEST_BATTERY_UPDATE_PATH)
    }
    val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    val percent = prefs.getInt(PreferenceKey.BATTERY_PERCENT_KEY, 0)
    val text =
        if (percent > 0) String.format(getString(R.string.phone_battery_percent), percent)
        else getString(R.string.phone_battery_unknown_short)

    val complicationData =
        ComplicationData.Builder(type)
            .setShortText(ComplicationText.plainText(text))
            .setIcon(createIcon(percent))
            .setTapAction(pendingIntent)
    if (type == ComplicationData.TYPE_RANGED_VALUE) {
      complicationData.setMaxValue(1.0f).setMinValue(0.0f).setValue((percent.toFloat()) / 100)
    }

    manager?.updateComplicationData(complicationId, complicationData.build())
  }

  private fun createIcon(batteryPercent: Int): Icon {
    val drawable = ContextCompat.getDrawable(this, R.drawable.ic_phone_battery)!!
    drawable.level = batteryPercent
    return Icon.createWithBitmap(drawable.toBitmap())
  }

  companion object {
    fun updateAll(context: Context) {
      ProviderUpdateRequester(
              context,
              ComponentName(context.packageName, PhoneBatteryComplicationProvider::class.java.name))
          .requestUpdateAll()
    }
  }
}
