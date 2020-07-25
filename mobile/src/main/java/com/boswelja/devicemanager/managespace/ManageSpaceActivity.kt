/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.managespace

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.BatterySyncWorker
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.devicemanager.databinding.ActivityManageSpaceBinding
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.widgetdb.WidgetDatabase
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class ManageSpaceActivity : BaseToolbarActivity() {

    private val coroutineScope = MainScope()

    private lateinit var binding: ActivityManageSpaceBinding
    private lateinit var activityManager: ActivityManager

    private val watchManager: WatchManager by lazy { WatchManager.get(this) }
    private var hasResetApp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        binding = ActivityManageSpaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbarLayout.toolbar, showTitle = true, showUpButton = true)
        setupButtons()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (hasResetApp) {
            clearData()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Initialise all the buttons we need.
     */
    private fun setupButtons() {
        binding.apply {
            clearCacheButton.setOnClickListener {
                clearCache()
            }
            resetSettingsButton.setOnClickListener {
                showSettingsResetConfirmation()
            }
            resetAppButton.setOnClickListener {
                showFullResetConfirmation()
            }
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
     * Prepares the progress bar for use again.
     * This resets progress to 0 and sets a new step count.
     */
    private fun initProgressBar(stepCount: Int) {
        binding.apply {
            progressBar.max = stepCount
            progressBar.progress = 0
        }
    }

    /**
     * Increments the progress bar by 1.
     */
    private fun incrementProgressBar() {
        binding.progressBar.progress += 1
    }

    /**
     * Updates the progress status text.
     * @param statusTextRes The resource identifier used to get a new [String] to use for progress status.
     */
    private fun setProgressStatus(@StringRes statusTextRes: Int) =
        setProgressStatus(getString(statusTextRes))

    /**
     * Updates the progress status text.
     * @param statusText The new [String] to use for progress status.
     */
    private fun setProgressStatus(statusText: String) {
        binding.progressStatus.text = statusText
    }

    /**
     * Shows a custom [AlertDialog] to confirm the user would like to reset their watch settings.
     * If the user chooses to reset their settings, the reset will be started from here.
     */
    private fun showSettingsResetConfirmation() {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.dialog_reset_settings_title)
            setMessage(R.string.dialog_reset_settings_message)
            setPositiveButton(R.string.dialog_button_reset) { _, _ ->
                doSettingsReset()
            }
            setNegativeButton(R.string.dialog_button_cancel) { dialog, _ ->
                dialog.cancel()
            }
        }.show()
    }

    /**
     * Shows a custom [AlertDialog] to confirm the user would like to fully reset Wearable Extensions.
     * If the user chooses to reset Wearable Extensions, the reset will be started from here.
     */
    private fun showFullResetConfirmation() {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.dialog_reset_app_title)
            setMessage(R.string.dialog_reset_app_message)
            setPositiveButton(R.string.dialog_button_reset) { _, _ ->
                doFullReset()
            }
            setNegativeButton(R.string.dialog_button_cancel) { dialog, _ ->
                dialog.cancel()
            }
        }.show()
    }

    /**
     * Calls the system's [ActivityManager] clearApplicationUserData()
     * to make sure we haven't missed anything.
     * @return true if successful, false otherwise.
     */
    private fun clearData(): Boolean = activityManager.clearApplicationUserData()

    /**
     * Attempts to clear all of Wearable Extension's cache.
     */
    private fun clearCache() {
        val cacheFiles = getFiles(codeCacheDir) + getFiles(cacheDir)
        if (cacheFiles.isNotEmpty()) {
            setButtonsEnabled(false)
            initProgressBar(cacheFiles.size)
            setProgressStatus(R.string.clear_cache_clearing)
            coroutineScope.launch(Dispatchers.IO) {
                for (cacheFile in cacheFiles) {
                    val deleted = cacheFile.delete()
                    withContext(Dispatchers.Main) {
                        if (deleted) {
                            incrementProgressBar()
                        } else {
                            Timber.w("Failed to delete cache file ${cacheFile.absolutePath}")
                            setProgressStatus(R.string.clear_cache_failed)
                            setButtonsEnabled(true)
                            return@withContext
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    setProgressStatus(R.string.clear_cache_success)
                    setButtonsEnabled(true)
                }
            }
        } else {
            initProgressBar(0)
            setProgressStatus(R.string.clear_cache_no_cache)
        }
    }

    /**
     * Attempts to reset settings for each watch registered with Wearable Extensions.
     */
    private fun doSettingsReset() {
        setButtonsEnabled(false)
        coroutineScope.launch(Dispatchers.IO) {
            val registeredWatches = watchManager.getRegisteredWatches()
            if (registeredWatches.isNotEmpty()) {
                initProgressBar(registeredWatches.count())
                for (watch in registeredWatches) {
                    withContext(Dispatchers.Main) {
                        setProgressStatus(getString(R.string.reset_settings_resetting_for, watch.name))
                    }
                    val success = resetWatchPreferences(watch)
                    if (success) {
                        withContext(Dispatchers.Main) {
                            incrementProgressBar()
                        }
                    } else {
                        binding.progressStatus.text = getString(R.string.reset_settings_failed_for, watch.name)
                        setButtonsEnabled(true)
                        return@launch
                    }
                }
                withContext(Dispatchers.Main) {
                    setProgressStatus(R.string.reset_settings_success)
                    setButtonsEnabled(true)
                }
            } else {
                withContext(Dispatchers.Main) {
                    initProgressBar(0)
                    setProgressStatus(R.string.reset_settings_failed)
                    setButtonsEnabled(true)
                }
            }
        }
    }

    /**
     * Attempts a full reset of Wearable Extensions.
     */
    private fun doFullReset() {
        setButtonsEnabled(false)
        coroutineScope.launch(Dispatchers.IO) {
            var totalSteps = DATABASE_COUNT + PREFERENCE_STORE_COUNT
            initProgressBar(totalSteps)
            val registeredWatches = watchManager.getRegisteredWatches()
            if (registeredWatches.isNotEmpty()) {
                totalSteps += (registeredWatches.count() * 3)
                withContext(Dispatchers.Main) {
                    initProgressBar(totalSteps)
                }
                for (watch in registeredWatches) {
                    withContext(Dispatchers.Main) {
                        setProgressStatus(getString(R.string.reset_app_resetting_for, watch.name))
                    }
                    val success = resetWatchPreferences(watch)
                    if (success) {
                        withContext(Dispatchers.Main) {
                            incrementProgressBar()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            setProgressStatus(getString(R.string.reset_app_failed_for, watch.name))
                            setButtonsEnabled(true)
                        }
                        return@launch
                    }
                    try {
                        Tasks.await(watchManager.requestResetWatch(watch.id))
                        withContext(Dispatchers.Main) {
                            incrementProgressBar()
                        }
                    } catch (e: ApiException) {
                        initProgressBar(0)
                        setProgressStatus(getString(R.string.reset_app_failed_for, watch.name))
                        setButtonsEnabled(true)
                        return@launch
                    }
                    watchManager.forgetWatch(watch.id)
                    withContext(Dispatchers.Main) {
                        incrementProgressBar()
                    }
                }
            }
            withContext(Dispatchers.Main) {
                setProgressStatus(getString(R.string.reset_app_resetting_database_for, getString(R.string.database_name_battery_stats)))
            }
            WatchBatteryStatsDatabase.open(this@ManageSpaceActivity).apply {
                clearAllTables()
            }.also {
                it.close()
            }
            withContext(Dispatchers.Main) {
                incrementProgressBar()
                setProgressStatus(getString(R.string.reset_app_resetting_database_for, getString(R.string.database_name_widget)))
            }
            WidgetDatabase.open(this@ManageSpaceActivity).apply {
                clearAllTables()
            }.also {
                it.close()
            }
            withContext(Dispatchers.Main) {
                incrementProgressBar()
                setProgressStatus(getString(R.string.reset_app_resetting_database_for, getString(R.string.database_name_main)))
            }
            WatchDatabase.get(this@ManageSpaceActivity).apply {
                clearAllTables()
            }.also {
                it.close()
            }
            withContext(Dispatchers.Main) {
                incrementProgressBar()
                setProgressStatus(R.string.reset_app_resetting_preferences)
            }
            PreferenceManager.getDefaultSharedPreferences(this@ManageSpaceActivity).edit(commit = true) {
                clear()
            }
            withContext(Dispatchers.Main) {
                incrementProgressBar()
                setProgressStatus(R.string.reset_app_success)
                setButtonsEnabled(true)
            }
            hasResetApp = true
        }
    }

    /**
     * Resets preferences for a given [Watch], and stops any services or workers that may need stopping.
     * @param watch The [Watch] to clear preferences for.
     * @return true if preferences were successfully cleared, false otherwise.
     */
    private suspend fun resetWatchPreferences(watch: Watch): Boolean {
        val success = watchManager.clearPreferencesForWatch(watch.id)
        if (watch.batterySyncWorkerId != null) {
            BatterySyncWorker.stopWorker(this, watch.id)
        }
        return success
    }

    /**
     * Recursive function to get an [Array] of all the files contained within the given [File].
     * @param file The [File] to return children of.
     * @return An [Array] of all the files found inside the given [File].
     */
    private fun getFiles(file: File): Array<File> {
        val files = ArrayList<File>()
        if (file.isDirectory) {
            Timber.i("${file.absolutePath} is a directory")
            val innerFiles = file.listFiles()
            if (innerFiles != null && innerFiles.isNotEmpty()) {
                for (innerFile in innerFiles) {
                    files.addAll(getFiles(innerFile))
                }
            } else {
                Timber.i("${file.absolutePath} has no inner files")
            }
        } else {
            Timber.i("${file.absolutePath} is not a directory")
            files.add(file)
        }
        return files.toTypedArray()
    }

    companion object {
        private const val DATABASE_COUNT = 3
        private const val PREFERENCE_STORE_COUNT = 1
    }
}
