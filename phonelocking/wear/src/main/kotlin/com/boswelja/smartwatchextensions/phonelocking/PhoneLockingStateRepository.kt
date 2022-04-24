package com.boswelja.smartwatchextensions.phonelocking

import kotlinx.coroutines.flow.Flow

/**
 * A repository for managing [PhoneLockingState].
 */
interface PhoneLockingStateRepository {

    /**
     * Flows the current [PhoneLockingState].
     */
    fun getPhoneLockingState(): Flow<PhoneLockingState>

    /**
     * Update the current [PhoneLockingState].
     * @param block Provides the current phone locking state. The updated state should be returned from this function.
     */
    suspend fun updatePhoneLockingState(block: (PhoneLockingState) -> PhoneLockingState)
}
