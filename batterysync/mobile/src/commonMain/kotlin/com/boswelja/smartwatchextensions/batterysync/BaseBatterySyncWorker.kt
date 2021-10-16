package com.boswelja.smartwatchextensions.batterysync

expect abstract class BaseBatterySyncWorker {
    abstract suspend fun onSendBatteryStats(
        targetUid: String,
        batteryStats: BatteryStats
    ): Boolean
}
