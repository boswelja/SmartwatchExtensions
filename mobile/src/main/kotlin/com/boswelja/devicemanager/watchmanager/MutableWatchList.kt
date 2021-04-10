package com.boswelja.devicemanager.watchmanager

import androidx.lifecycle.MediatorLiveData
import com.boswelja.devicemanager.watchmanager.item.Watch

class MutableWatchList : MediatorLiveData<List<Watch>>() {

    fun postValueForPlatform(watches: List<Watch>) {
        watches.firstOrNull()?.let { watch ->
            val platform = watch.platform
            if (watches.any { it.platform != platform })
                throw IllegalArgumentException("Platforms of all watches must match")
            // Remove all watches for the target platform.
            val strippedWatches = (value ?: emptyList()).filter { it.platform != platform }
            postValue(strippedWatches + watches)
        }
    }
}
