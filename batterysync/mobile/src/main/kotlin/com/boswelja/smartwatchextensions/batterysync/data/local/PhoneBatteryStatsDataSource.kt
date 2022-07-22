package com.boswelja.smartwatchextensions.batterysync.data.local

import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.batteryStats

class PhoneBatteryStatsDataSource(
    private val context: Context
) {
    /**
     * Retrieves an up to date [BatteryStats] for the local device, or null if something went wrong.
     */
    fun getBatteryStats(): BatteryStats? {
        return context.batteryStats()
    }
}
