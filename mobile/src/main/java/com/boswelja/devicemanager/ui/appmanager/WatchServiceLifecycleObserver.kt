package com.boswelja.devicemanager.ui.appmanager

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class WatchServiceLifecycleObserver(private val viewModel: AppManagerViewModel) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        viewModel.startAppManagerService()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        viewModel.tryStopAppManagerService()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        viewModel.canStopAppManagerService = true
        viewModel.tryStopAppManagerService()
    }

}