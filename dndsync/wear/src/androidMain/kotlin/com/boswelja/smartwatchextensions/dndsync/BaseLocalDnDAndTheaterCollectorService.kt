package com.boswelja.smartwatchextensions.dndsync

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * A [BaseLocalDnDListenerService] that also collects Theater Mode changes.
 */
abstract class BaseLocalDnDAndTheaterCollectorService : BaseLocalDnDListenerService() {

    /**
     * Called when the device's Theater Mode state changes.
     * @param theaterState The new DnD state.
     */
    abstract suspend fun onTheaterChanged(theaterState: Boolean)

    override fun onCreate() {
        super.onCreate()
        lifecycleScope.launch {
            theaterMode().collect { theaterMode ->
                onTheaterChanged(theaterMode)
            }
        }
    }
}
