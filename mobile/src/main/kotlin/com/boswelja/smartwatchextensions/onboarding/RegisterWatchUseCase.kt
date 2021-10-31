package com.boswelja.smartwatchextensions.onboarding

import com.boswelja.smartwatchextensions.devicemanagement.RegistrationMessageHandler
import com.boswelja.smartwatchextensions.devicemanagement.WatchRepository
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.flow.Flow

/**
 * A use case for onboarding watch registration.
 */
class RegisterWatchUseCase(
    private val watchRepository: WatchRepository,
    private val registrationMessageHandler: RegistrationMessageHandler,
    private val registrationTimeout: Long
) {

    /**
     * Flow the watches available to be registered.
     */
    fun availableWatches(): Flow<List<Watch>> = watchRepository.availableWatches

    /**
     * Attempt to register the given watch.
     * @param watch The watch to register.
     * @return true if registration was successful, false otherwise.
     */
    suspend fun registerWatch(watch: Watch): Boolean {
        watchRepository.registerWatch(watch)
        val success = registrationMessageHandler.sendRegisteredMessage(watch.uid)
        return success &&
            registrationMessageHandler.awaitRegistrationAcknowledged(watch.uid, registrationTimeout)
    }
}
