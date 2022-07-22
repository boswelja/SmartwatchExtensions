package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository

/**
 * Retrieves an up to date [BatteryStats] for the local device
 */
class GetPhoneBatteryStats(
    private val repository: BatteryStatsRepository
) {
    operator fun invoke(): Result<BatteryStats> {
        return runCatching {
            val batteryStats = repository.getBatteryStatsForPhone()
            checkNotNull(batteryStats) { "BatteryStats must not be null!" }
        }
    }
}
