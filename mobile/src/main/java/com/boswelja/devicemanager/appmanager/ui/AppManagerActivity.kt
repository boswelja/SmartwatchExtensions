package com.boswelja.devicemanager.appmanager.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.commit
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.appmanager.State
import com.boswelja.devicemanager.common.ui.activity.BaseWatchPickerActivity
import com.boswelja.devicemanager.common.ui.fragment.LoadingFragment
import com.boswelja.devicemanager.databinding.ActivityAppManagerBinding
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class AppManagerActivity : BaseWatchPickerActivity() {

    private var shouldAnimateTransitions = false

    private val viewModel: AppManagerViewModel by viewModels()

    private val loadingFragment = LoadingFragment()
    private val appListFragment by lazy { AppListFragment() }

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
                State.CONNECTING, State.LOADING_APPS -> showLoadingFragment()
                State.READY -> showAppManagerFragment()
                State.DISCONNECTED -> notifyDisconnected()
                else -> Timber.e("App Manager state is null")
            }
        }

        viewModel.progress.observe(this) {
            loadingFragment.progress.postValue(it)
        }
    }

    /** Shows a [LoadingFragment]. */
    private fun showLoadingFragment() {
        Timber.i("showLoadingFragment() called")
        supportFragmentManager.commit {
            if (shouldAnimateTransitions) {
                setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            } else {
                shouldAnimateTransitions = true
            }
            replace(R.id.fragment_holder, loadingFragment)
        }
    }

    private fun notifyDisconnected() {
        disconnectedSnackbar.show()
    }

    /** Shows the [AppListFragment]. */
    private fun showAppManagerFragment() {
        Timber.i("showAppManagerFragment() called")
        supportFragmentManager.commit {
            if (shouldAnimateTransitions) {
                setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            } else {
                shouldAnimateTransitions = true
            }
            replace(R.id.fragment_holder, appListFragment)
        }
    }
}
