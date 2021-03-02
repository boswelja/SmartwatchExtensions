package com.boswelja.devicemanager.appmanager.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.findNavController
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.appmanager.State
import com.boswelja.devicemanager.common.ui.activity.BaseWatchPickerActivity
import com.boswelja.devicemanager.databinding.ActivityAppManagerBinding
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class AppManagerActivity : BaseWatchPickerActivity() {

    private val viewModel: AppManagerViewModel by viewModels()
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    private val disconnectedSnackbar by lazy {
        Snackbar.make(
            findViewById(android.R.id.content),
            R.string.app_manager_disconnected,
            Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.button_retry) {
            viewModel.startAppManagerService()
        }
    }

    private lateinit var binding: ActivityAppManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate() called")

        binding = ActivityAppManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWatchPickerSpinner(binding.toolbarLayout.toolbar, showUpButton = true)

        viewModel.state.observe(this) {
            Timber.d("State = ${it.name}")
            when (it) {
                State.CONNECTING, State.LOADING_APPS -> showLoading()
                State.READY -> showAppList()
                State.DISCONNECTED -> notifyDisconnected()
                State.ERROR -> showError()
                else -> Timber.e("App Manager state is null")
            }
        }
    }

    private fun showLoading() {
        Timber.i("showLoading() called")
        if (navController.currentDestination?.id != R.id.loadingFragment) {
            navController.navigate(R.id.to_loadingFragment)
        }
    }

    private fun showError() {
        Timber.i("showError() called")
        if (navController.currentDestination?.id != R.id.errorFragment) {
            navController.navigate(R.id.to_errorFragment)
        }
    }

    private fun showAppList() {
        Timber.i("showAppList() called")
        if (navController.currentDestination?.id == R.id.loadingFragment) {
            navController.navigate(R.id.loadingFragment_to_appListFragment)
        }
    }

    private fun notifyDisconnected() {
        disconnectedSnackbar.show()
    }
}
