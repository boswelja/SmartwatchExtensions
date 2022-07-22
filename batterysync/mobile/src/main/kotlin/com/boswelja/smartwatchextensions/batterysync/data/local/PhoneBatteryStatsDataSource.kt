package com.boswelja.smartwatchextensions.batterysync.data.local

import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.batteryStats

class PhoneBatteryStatsDataSource(
    private val context: Context
) {
    fun getBatteryStats(): BatteryStats? {
        return context.batteryStats()
    }
}
