package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetBatterySyncEnabled(
    private val getBatterySyncConfig: GetBatterySyncConfig
) {

    operator fun invoke(): Flow<Result<Boolean>> {
        return getBatterySyncConfig()
            .map { result ->
                result.map { batterySyncConfig -> batterySyncConfig.batterySyncEnabled }
            }
    }
}
