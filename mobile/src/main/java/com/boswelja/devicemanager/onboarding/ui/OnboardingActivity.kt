package com.boswelja.devicemanager.onboarding.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.navigation.compose.rememberNavController
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.LifecycleAwareTimer
import com.boswelja.devicemanager.common.ui.AppTheme
import com.boswelja.devicemanager.common.ui.Crossflow
import com.boswelja.devicemanager.common.ui.UpNavigationAppBar
import com.boswelja.devicemanager.main.MainActivity
import com.boswelja.devicemanager.watchmanager.ui.register.RegisterWatchViewModel
import com.boswelja.devicemanager.watchmanager.ui.register.RegisterWatchesScreen

class OnboardingActivity : AppCompatActivity() {

    private val registerWatchViewModel: RegisterWatchViewModel by viewModels()
    private val customTabIntent = CustomTabsIntent.Builder().setShowTitle(true).build()
    private val availableWatchUpdateTimer = LifecycleAwareTimer(TIMER_UPDATE_SECONDS) {
        registerWatchViewModel.refreshData()
    }

    private var currentDestination by mutableStateOf(Destination.WELCOME)

    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val navController = rememberNavController()

                Scaffold(
                    topBar = {
                        UpNavigationAppBar(
                            onNavigateUp = {
                                if (!navController.popBackStack()) finish()
                            }
                        )
                    },
                    floatingActionButtonPosition = FabPosition.End,
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = { Text(stringResource(R.string.button_next)) },
                            icon = { Icon(Icons.Outlined.NavigateNext, null) },
                            onClick = { navigateNext() }
                        )
                    }
                ) {
                    OnboardingScreen(currentDestination = currentDestination)
                }
            }
        }

        lifecycle.addObserver(availableWatchUpdateTimer)

        registerWatchViewModel.availableWatches.observe(this) {
            it.forEach { watch ->
                registerWatchViewModel.registerWatch(watch)
            }
        }
    }

    private fun navigateNext() {
        when (currentDestination) {
            Destination.WELCOME -> currentDestination = Destination.SHARE_USAGE_STATS
            Destination.SHARE_USAGE_STATS -> currentDestination = Destination.REGISTER_WATCHES
            Destination.REGISTER_WATCHES -> {
                if (!registerWatchViewModel.registeredWatches.value.isNullOrEmpty()) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    @Composable
    fun OnboardingScreen(currentDestination: Destination) {
        val registeredWatches by registerWatchViewModel.registeredWatches.observeAsState()
        Crossflow(targetState = currentDestination) {
            when (it) {
                Destination.WELCOME -> WelcomeScreen()
                Destination.SHARE_USAGE_STATS -> {
                    UsageStatsScreen(
                        onShowPrivacyPolicy = {
                            customTabIntent.launchUrl(
                                this@OnboardingActivity,
                                getString(R.string.privacy_policy_url).toUri()
                            )
                        }
                    )
                }
                Destination.REGISTER_WATCHES -> RegisterWatchesScreen(registeredWatches)
            }
        }
    }

    enum class Destination {
        WELCOME,
        SHARE_USAGE_STATS,
        REGISTER_WATCHES
    }

    companion object {
        private const val TIMER_UPDATE_SECONDS: Long = 5
    }
}
