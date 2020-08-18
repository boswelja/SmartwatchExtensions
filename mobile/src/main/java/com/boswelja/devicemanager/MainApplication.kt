/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.app.Application
import android.content.Intent
import com.boswelja.devicemanager.bootorupdate.BootOrUpdateHandlerService
import com.boswelja.devicemanager.bootorupdate.updater.Updater
import com.boswelja.devicemanager.common.Compat
import timber.log.Timber

class MainApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }

    ensureEnvironmentUpdated()
  }

  /** Checks the environment is up to date, otherwise starts a [BootOrUpdateHandlerService]. */
  private fun ensureEnvironmentUpdated() {
    val updater = Updater(this)
    if (updater.needsUpdate) {
      Timber.i("Starting updater service")
      Intent(this, BootOrUpdateHandlerService::class.java)
          .apply { action = Intent.ACTION_MY_PACKAGE_REPLACED }
          .also { Compat.startForegroundService(this, it) }
    }
  }
}
