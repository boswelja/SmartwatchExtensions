package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.BatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository

class StoreBatteryStatsForWatch(
    private val batteryStatsRepository: BatteryStatsRepository,
    private val batterySyncNotificationHandler: BatterySyncNotificationHandler
) {
    suspend operator fun invoke(watchId: String, batteryStats: BatteryStats) {
        batteryStatsRepository.updateStatsFor(watchId, batteryStats)
        batterySyncNotificationHandler.handleNotificationsFor(watchId, batteryStats)
    }
}
