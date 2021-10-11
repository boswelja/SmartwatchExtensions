package com.boswelja.smartwatchextensions.dndsync

import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * A [LifecycleService] that collects the device's DnD state throughout it's lifecycle.
 */
abstract class BaseLocalDnDCollectorService : LifecycleService() {

    /**
     * Called when the device's DnD state changes.
     * @param dndState The new DnD state.
     */
    abstract suspend fun onDnDChanged(dndState: Boolean)

    override fun onCreate() {
        super.onCreate()
        lifecycleScope.launch {
            dndState().collect { dndState ->
                onDnDChanged(dndState)
            }
        }
    }
}
