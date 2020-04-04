package com.boswelja.devicemanager

import android.app.Application
import androidx.appcompat.app.AlertDialog
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainApplication : Application() {

    private lateinit var environmentUpdater: EnvironmentUpdater
    private lateinit var dialog: AlertDialog

    private val serviceConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            completeFullUpdate(service)
        }

        override fun onWatchManagerUnbound() {}
    }

    override fun onCreate() {
        super.onCreate()

        handleUpdates()
    }

    private fun handleUpdates() {
        environmentUpdater = EnvironmentUpdater(this)
        when (environmentUpdater.doUpdate()) {
            EnvironmentUpdater.UPDATE_SUCCESS -> {
                //ChangelogDialogFragment().show(supportFragmentManager.beginTransaction(), "ChangelogDialogFragment")
            }
            EnvironmentUpdater.NEEDS_FULL_UPDATE -> {
                startFullUpdate()
            }
        }
    }

    private fun startFullUpdate() {
        dialog = MaterialAlertDialogBuilder(this).apply {
            setTitle(getString(R.string.update_dialog_title, getString(R.string.app_name)))
            setView(R.layout.common_dialog_progressbar)
            setCancelable(false)
        }.show()
        WatchConnectionService.bind(this@MainApplication, serviceConnection)
    }

    private fun completeFullUpdate(service: WatchConnectionService) {
        MainScope().launch {
            environmentUpdater.doFullUpdate(service)
            withContext(Dispatchers.Main) {
                dialog.cancel()
                unbindService(serviceConnection)
            }
        }
    }
}