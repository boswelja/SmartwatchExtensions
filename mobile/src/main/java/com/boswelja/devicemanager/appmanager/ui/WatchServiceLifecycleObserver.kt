/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.appmanager.ui

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class WatchServiceLifecycleObserver(private val viewModel: AppManagerViewModel) :
    DefaultLifecycleObserver {

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
