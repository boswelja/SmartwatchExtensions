/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.managespace.ui

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.analytics.Analytics
import com.boswelja.devicemanager.batterysync.BatterySyncWorker
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.devicemanager.common.ui.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivityManageSpaceBinding
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.boswelja.devicemanager.widget.database.WidgetDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageSpaceActivity : BaseToolbarActivity() {

    private val coroutineScope = MainScope()

    private lateinit var binding: ActivityManageSpaceBinding
    private lateinit var activityManager: ActivityManager

    private val viewModel: ManageSpaceViewModel by viewModels()
    private val analytics: Analytics by lazy { Analytics() }
    private val watchManager: WatchManager by lazy { WatchManager.getInstance(this) }
    private var hasResetApp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        binding = ActivityManageSpaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbarLayout.toolbar, showTitle = true, showUpButton = true)
        setupButtons()
        viewModel
    }

    override fun onDestroy() {
        super.onDestroy()
        if (hasResetApp) {
            clearData()
        }
    }

    /** Initialise all the buttons we need. */
    private fun setupButtons() {
        binding.apply {
            clearCacheButton.setOnClickListener { clearCache() }
            resetSettingsButton.setOnClickListener { showSettingsResetConfirmation() }
            resetAppButton.setOnClickListener { showFullResetConfirmation() }
        }
    }

    /**
     * Sets whether the user can interact with any of the buttons on screen.
     * @param enabled Whether the user can interact with buttons.
     */
    private fun setButtonsEnabled(enabled: Boolean) {
        binding.apply {
            clearCacheButton.isEnabled = enabled
            resetSettingsButton.isEnabled = enabled
            resetAppButton.isEnabled = enabled
        }
    }

    /**
     * Prepares the progress bar for use again. This resets progress to 0 and sets a new step count.
     */
    private fun initProgressBar(stepCount: Int) {
        binding.apply {
            progressBar.max = stepCount
            progressBar.progress = 0
        }
    }

    /** Increments the progress bar by 1. */
    private fun incrementProgressBar() {
        binding.progressBar.progress += 1
    }

    /**
     * Updates the progress status text.
     * @param statusText The new [String] to use for progress status.
     */
    private fun setProgressStatus(statusText: String) {
        binding.progressStatus.text = statusText
    }

    /**
     * Shows a custom [AlertDialog] to confirm the user would like to reset their watch settings. If
     * the user chooses to reset their settings, the reset will be started from here.
     */
    private fun showSettingsResetConfirmation() {
        AlertDialog.Builder(this)
            .apply {
                setTitle(R.string.dialog_reset_settings_title)
                setMessage(R.string.dialog_reset_settings_message)
                setPositiveButton(R.string.dialog_button_reset) { _, _ -> doSettingsReset() }
                setNegativeButton(R.string.dialog_button_cancel) { dialog, _ -> dialog.cancel() }
            }
            .show()
    }

    /**
     * Shows a custom [AlertDialog] to confirm the user would like to fully reset Wearable
     * Extensions. If the user chooses to reset Wearable Extensions, the reset will be started from
     * here.
     */
    private fun showFullResetConfirmation() {
        AlertDialog.Builder(this)
            .apply {
                setTitle(R.string.dialog_reset_app_title)
                setMessage(R.string.dialog_reset_app_message)
                setPositiveButton(R.string.dialog_button_reset) { _, _ -> doFullReset() }
                setNegativeButton(R.string.dialog_button_cancel) { dialog, _ -> dialog.cancel() }
            }
            .show()
    }

    /**
     * Calls the system's [ActivityManager] clearApplicationUserData() to make sure we haven't
     * missed anything.
     * @return true if successful, false otherwise.
     */
    private fun clearData(): Boolean {
        analytics.logStorageManagerAction("clearData")
        return activityManager.clearApplicationUserData()
    }

    /** Attempts to clear all of Wearable Extension's cache. */
    private fun clearCache() {
        ClearCacheBottomSheet()
            .show(supportFragmentManager, ClearCacheBottomSheet::class.simpleName)
    }

    /** Attempts to reset settings for each watch registered with Wearable Extensions. */
    private fun doSettingsReset() {
        setButtonsEnabled(false)
        coroutineScope.launch(Dispatchers.IO) {
            val registeredWatches = watchManager.registeredWatches.value!!
            if (registeredWatches.isNotEmpty()) {
                analytics.logStorageManagerAction("clearSettings")
                initProgressBar(registeredWatches.count())
                for (watch in registeredWatches) {
                    withContext(Dispatchers.Main) {
                        setProgressStatus(
                            getString(R.string.reset_settings_resetting_for, watch.name)
                        )
                    }
                    resetWatchPreferences(watch)
                    withContext(Dispatchers.Main) { incrementProgressBar() }
                }
                withContext(Dispatchers.Main) {
                    setProgressStatus(getString(R.string.reset_settings_success))
                    setButtonsEnabled(true)
                }
            } else {
                withContext(Dispatchers.Main) {
                    initProgressBar(0)
                    setProgressStatus(getString(R.string.reset_settings_failed))
                    setButtonsEnabled(true)
                }
            }
        }
    }

    /** Attempts a full reset of Wearable Extensions. */
    private fun doFullReset() {
        setButtonsEnabled(false)
        coroutineScope.launch(Dispatchers.IO) {
            analytics.logStorageManagerAction("fullReset")
            var totalSteps = DATABASE_COUNT + PREFERENCE_STORE_COUNT
            initProgressBar(totalSteps)
            val registeredWatches = watchManager.registeredWatches.value!!
            if (registeredWatches.isNotEmpty()) {
                totalSteps += (registeredWatches.count() * 3)
                withContext(Dispatchers.Main) { initProgressBar(totalSteps) }
                for (watch in registeredWatches) {
                    withContext(Dispatchers.Main) {
                        setProgressStatus(getString(R.string.reset_app_resetting_for, watch.name))
                    }
                    resetWatchPreferences(watch)
                    withContext(Dispatchers.Main) { incrementProgressBar() }
                    try {
                        watchManager.requestResetWatch(watch)
                        withContext(Dispatchers.Main) { incrementProgressBar() }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            initProgressBar(0)
                            setProgressStatus(getString(R.string.reset_app_failed_for, watch.name))
                            setButtonsEnabled(true)
                        }
                        return@launch
                    }
                    watchManager.forgetWatch(watch)
                    withContext(Dispatchers.Main) { incrementProgressBar() }
                }
            }
            withContext(Dispatchers.Main) {
                setProgressStatus(
                    getString(
                        R.string.reset_app_resetting_database_for,
                        getString(R.string.database_name_battery_stats)
                    )
                )
            }
            WatchBatteryStatsDatabase.get(this@ManageSpaceActivity).clearAllTables()
            withContext(Dispatchers.Main) {
                incrementProgressBar()
                setProgressStatus(
                    getString(
                        R.string.reset_app_resetting_database_for,
                        getString(R.string.database_name_widget)
                    )
                )
            }
            WidgetDatabase.open(this@ManageSpaceActivity).apply { clearAllTables() }.also {
                it.close()
            }
            withContext(Dispatchers.Main) {
                incrementProgressBar()
                setProgressStatus(
                    getString(
                        R.string.reset_app_resetting_database_for,
                        getString(R.string.database_name_main)
                    )
                )
            }
            WatchDatabase.getInstance(this@ManageSpaceActivity).apply { clearAllTables() }.also {
                it.close()
            }
            withContext(Dispatchers.Main) {
                incrementProgressBar()
                setProgressStatus(getString(R.string.reset_app_resetting_preferences))
            }
            PreferenceManager.getDefaultSharedPreferences(this@ManageSpaceActivity).edit(
                commit = true
            ) { clear() }
            withContext(Dispatchers.Main) {
                incrementProgressBar()
                setProgressStatus(getString(R.string.reset_app_success))
                setButtonsEnabled(true)
            }
            hasResetApp = true
        }
    }

    /**
     * Resets preferences for a given [Watch], and stops any running services or workers.
     * @param watch The [Watch] to clear preferences for.
     */
    private fun resetWatchPreferences(watch: Watch) {
        coroutineScope.launch {
            watchManager.resetWatchPreferences(watch)
            BatterySyncWorker.stopWorker(this@ManageSpaceActivity, watch.id)
        }
    }

    companion object {
        private const val DATABASE_COUNT = 3
        private const val PREFERENCE_STORE_COUNT = 1
    }
}
