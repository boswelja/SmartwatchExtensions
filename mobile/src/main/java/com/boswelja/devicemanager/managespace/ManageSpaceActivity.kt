package com.boswelja.devicemanager.managespace

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.devicemanager.databinding.ActivityManageSpaceBinding
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import com.boswelja.devicemanager.watchconnectionmanager.database.WatchDatabase
import com.boswelja.devicemanager.widgetdb.WidgetDatabase
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Tasks
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class ManageSpaceActivity : AppCompatActivity() {

    private val watchManagerConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            watchConnectionManager = service
        }

        override fun onWatchManagerUnbound() {
            watchConnectionManager = null
        }
    }

    private val coroutineScope = MainScope()

    private lateinit var binding: ActivityManageSpaceBinding
    private lateinit var activityManager: ActivityManager

    private var watchConnectionManager: WatchConnectionService? = null
    private var hasResetApp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_space)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        setupButtons()
    }

    override fun onStart() {
        super.onStart()
        WatchConnectionService.bind(this, watchManagerConnection)
    }

    override fun onStop() {
        super.onStop()
        if (!hasResetApp) {
            unbindService(watchManagerConnection)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (hasResetApp) {
            tryClearData()
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

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.apply {
            clearCacheButton.isEnabled = enabled
            resetSettingsButton.isEnabled = enabled
            resetAppButton.isEnabled = enabled
        }
    }

    private fun initProgressBar(stepCount: Int) {
        binding.apply {
            progressBar.max = stepCount
            progressBar.progress = 0
        }
    }

    private fun showSettingsResetConfirmation() {
        MaterialAlertDialogBuilder(this, R.style.AppTheme_AlertDialog).apply {
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

    private fun showFullResetConfirmation() {
        MaterialAlertDialogBuilder(this, R.style.AppTheme_AlertDialog).apply {
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

    private fun tryClearData(): Boolean {
        return activityManager.clearApplicationUserData()
    }

    private fun clearCache() {
        val cacheFiles = getFiles(codeCacheDir) + getFiles(cacheDir)
        if (cacheFiles.isNotEmpty()) {
            setButtonsEnabled(false)
            initProgressBar(cacheFiles.size)
            binding.progressStatus.setText(R.string.clear_cache_clearing)
            coroutineScope.launch(Dispatchers.IO) {
                for (cacheFile in cacheFiles) {
                    val deleted = cacheFile.delete()
                    withContext(Dispatchers.Main) {
                        if (deleted) {
                            binding.progressBar.progress += 1
                        } else {
                            Timber.w("Failed to delete cache file ${cacheFile.absolutePath}")
                            binding.progressStatus.setText(R.string.clear_cache_failed)
                            setButtonsEnabled(true)
                            return@withContext
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    binding.progressStatus.setText(R.string.clear_cache_success)
                    setButtonsEnabled(true)
                }
            }
        } else {
            initProgressBar(0)
            binding.progressStatus.setText(R.string.clear_cache_no_cache)
        }
    }

    private fun doSettingsReset() {
        coroutineScope.launch(Dispatchers.IO) {
            val registeredWatches = watchConnectionManager?.getRegisteredWatches()
            if (registeredWatches != null && registeredWatches.isNotEmpty()) {
                initProgressBar(registeredWatches.count())
                for (watch in registeredWatches) {
                    withContext(Dispatchers.Main) {
                        binding.progressStatus.text = getString(R.string.reset_settings_resetting_for, watch.name)
                    }
                    val success = watchConnectionManager!!.clearPreferencesForWatch(watch.id)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            binding.progressBar.progress += 1
                        } else {
                            binding.progressStatus.text = getString(R.string.reset_settings_failed_for, watch.name)
                        }
                    }
                    if (!success) return@launch
                }
                withContext(Dispatchers.Main) {
                    binding.progressStatus.setText(R.string.reset_settings_success)
                }
            } else {
                withContext(Dispatchers.Main) {
                    binding.progressBar.progress = 0
                    binding.progressStatus.setText(R.string.reset_settings_failed)
                }
            }
        }
    }

    private fun doFullReset() {
        coroutineScope.launch(Dispatchers.IO) {
            setButtonsEnabled(false)
            var totalSteps = DATABASE_COUNT + PREFERENCE_STORE_COUNT
            withContext(Dispatchers.Main) {
                initProgressBar(totalSteps)
            }
            val registeredWatches = watchConnectionManager?.getRegisteredWatches()
            if (registeredWatches != null && registeredWatches.isNotEmpty()) {
                totalSteps += (registeredWatches.count() * 3)
                withContext(Dispatchers.Main) {
                    initProgressBar(totalSteps)
                }
                for (watch in registeredWatches) {
                    withContext(Dispatchers.Main) {
                        binding.progressStatus.text = getString(R.string.reset_app_resetting_for, watch.name)
                    }
                    watchConnectionManager!!.clearPreferencesForWatch(watch.id)
                    withContext(Dispatchers.Main) {
                        binding.progressBar.progress += 1
                    }
                    try {
                        Tasks.await(watchConnectionManager!!.resetWatch(watch.id))
                        withContext(Dispatchers.Main) {
                            binding.progressBar.progress += 1
                        }
                    } catch (e: ApiException) {
                        binding.progressStatus.text = getString(R.string.reset_app_failed_for, watch.name)
                        return@launch
                    }
                    watchConnectionManager!!.forgetWatch(watch.id)
                    withContext(Dispatchers.Main) {
                        binding.progressBar.progress += 1
                    }
                }
            }
            unbindService(watchManagerConnection)
            withContext(Dispatchers.Main) {
                binding.progressStatus.text = getString(R.string.reset_app_resetting_database_for, getString(R.string.database_name_battery_stats))
            }
            WatchBatteryStatsDatabase.open(this@ManageSpaceActivity).apply {
                clearAllTables()
            }.also {
                it.close()
            }
            withContext(Dispatchers.Main) {
                binding.progressBar.progress += 1
                binding.progressStatus.text = getString(R.string.reset_app_resetting_database_for, getString(R.string.database_name_widget))
            }
            WidgetDatabase.open(this@ManageSpaceActivity).apply {
                clearAllTables()
            }.also {
                it.close()
            }
            withContext(Dispatchers.Main) {
                binding.progressBar.progress += 1
                binding.progressStatus.text = getString(R.string.reset_app_resetting_database_for, getString(R.string.database_name_main))
            }
            WatchDatabase.open(this@ManageSpaceActivity).apply {
                clearAllTables()
            }.also {
                it.close()
            }
            withContext(Dispatchers.Main) {
                binding.progressBar.progress += 1
                binding.progressStatus.setText(R.string.reset_app_resetting_preferences)
            }
            PreferenceManager.getDefaultSharedPreferences(this@ManageSpaceActivity).edit(commit = true) {
                clear()
            }
            withContext(Dispatchers.Main) {
                binding.progressBar.progress += 1
                binding.progressStatus.setText(R.string.reset_app_success)
            }
            hasResetApp = true
        }
    }

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