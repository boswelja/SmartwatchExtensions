package com.boswelja.smartwatchextensions.dndsync

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * A [BaseLocalDnDCollectorService] that also collects Theater Mode changes.
 */
abstract class BaseLocalDnDAndTheaterCollectorService : BaseLocalDnDCollectorService() {

    /**
     * Called when the device's Theater Mode state changes.
     * @param theaterState The new DnD state.
     */
    abstract suspend fun onTheaterChanged(theaterState: Boolean)

    override fun onCreate() {
        super.onCreate()
        lifecycleScope.launch {
            contentResolver.theaterMode().collect { theaterMode ->
                onTheaterChanged(theaterMode)
            }
        }
    }
}
