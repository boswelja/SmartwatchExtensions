/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.batterysync

import com.google.android.gms.wearable.MessageEvent

open class BatteryStats(
    open val percent: Int,
    open val isCharging: Boolean,
    open val lastUpdatedMillis: Long = System.currentTimeMillis()
) {
  companion object {
    fun fromMessage(messageEvent: MessageEvent): BatteryStats {
      val message = String(messageEvent.data, Charsets.UTF_8)
      val messageSplit = message.split("|")
      val batteryPercent = messageSplit[0].toInt()
      val isWatchCharging = messageSplit[1] == true.toString()
      return BatteryStats(batteryPercent, isWatchCharging)
    }
  }
}
