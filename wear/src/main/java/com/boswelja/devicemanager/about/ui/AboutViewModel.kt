/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.about.ui

import androidx.lifecycle.ViewModel
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.common.Event

class AboutViewModel : ViewModel() {

  val version = BuildConfig.VERSION_NAME

  val openPlayStoreEvent = Event()
  val openAppInfoEvent = Event()

  fun fireOpenPlayStoreEvent() = openPlayStoreEvent.fire()
  fun fireOpenAppInfoEvent() = openAppInfoEvent.fire()
}
