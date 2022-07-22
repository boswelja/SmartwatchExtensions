package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository

class SetPhoneBatteryStats(
    private val batteryStatsRepository: BatteryStatsRepository
) {
    suspend operator fun invoke(batteryStats: BatteryStats) {
        batteryStatsRepository.updatePhoneBatteryStats(batteryStats)
    }
}
