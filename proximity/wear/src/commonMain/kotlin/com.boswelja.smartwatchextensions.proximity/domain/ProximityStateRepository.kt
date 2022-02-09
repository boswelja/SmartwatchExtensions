package com.boswelja.smartwatchextensions.proximity.domain

import kotlinx.coroutines.flow.Flow

/**
 * A repository for managing proximity related states.
 */
interface ProximityStateRepository {

    /**
     * Flow whether phone separation alerts are enabled.
     */
    fun getPhoneSeparationAlertEnabled(): Flow<Boolean>

    /**
     * Set whether phone separation alerts are enabled.
     */
    suspend fun setPhoneSeparationAlertEnabled(newValue: Boolean)
}
