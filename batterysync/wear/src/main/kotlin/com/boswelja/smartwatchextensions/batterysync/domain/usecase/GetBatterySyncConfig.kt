package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.domain.model.BatterySyncConfig
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatterySyncConfigRepository
import com.boswelja.smartwatchextensions.core.runCatching
import kotlinx.coroutines.flow.Flow

class GetBatterySyncConfig(
    private val batterySyncConfigRepository: BatterySyncConfigRepository
) {
    operator fun invoke(): Flow<Result<BatterySyncConfig>> {
        return batterySyncConfigRepository.getBatterySyncState()
            .runCatching()
    }
}
