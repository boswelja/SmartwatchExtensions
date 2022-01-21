package com.boswelja.smartwatchextensions.phonelocking

import kotlinx.coroutines.flow.Flow

interface PhoneLockingStateRepository {
    fun getPhoneLockingState(): Flow<PhoneLockingState>

    suspend fun updatePhoneLockingState(block: (PhoneLockingState) -> PhoneLockingState)
}
