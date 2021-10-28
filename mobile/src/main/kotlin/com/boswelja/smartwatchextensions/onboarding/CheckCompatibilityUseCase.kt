package com.boswelja.smartwatchextensions.onboarding

import android.content.Context
import com.boswelja.watchconnection.wearos.isWearOSAvailable

/**
 * A use case to aid checking whether the current device supports any platforms we support.
 */
class CheckCompatibilityUseCase(
    private val context: Context
) {
    /**
     * Check whether any of the platforms we support are supported on this device.
     * @return true if there were any platforms supported, false otherwise.
     */
    suspend fun isCompatibleWithAnyPlatform(): Boolean {
        return context.isWearOSAvailable()
    }
}
